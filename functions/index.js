process.env.GOOGLE_APPLICATION_CREDENTIALS =
  "./sistema-autenticacion-firebase-74577d404c.json";

const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");
const FormData = require("form-data");

admin.initializeApp();

const db = admin.firestore();
const bucketName = "sistema-autenticacion-facial.appspot.com";

// Helper function to sleep for a given number of milliseconds
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

// Helper function to process a single request
const processRequest = async (request) => {
  try {
    if (!Array.isArray(request.photoPaths) || request.photoPaths.length === 0) {
      console.error(`Request for ${request.dni} has no valid photoPaths.`);
      return; // Skip this request
    }

    const imagePromises = request.photoPaths.map(async (path) => {
      const file = admin.storage().bucket(bucketName).file(path);
      const [url] = await file.getSignedUrl({
        action: "read",
        expires: "03-09-2491",
      });

      const response = await axios.get(url, {responseType: "arraybuffer"});
      return {buffer: Buffer.from(response.data), path};
    });

    const images = await Promise.all(imagePromises);

    const formData = new FormData();
    formData.append("name", request.dni);

    if (images[0]) {
      formData.append("image1", images[0].buffer, images[0].path);
    }
    if (images[1]) {
      formData.append("image2", images[1].buffer, images[1].path);
    }
    if (images[2]) {
      formData.append("image3", images[2].buffer, images[2].path);
    }

    const apiResponse = await axios.post(
        "https://Biogin.pythonanywhere.com/train",
        formData,
        {
          headers: {
            ...formData.getHeaders(),
          },
        },
    );

    console.log(`Training result for ${request.dni}:
         ${JSON.stringify(apiResponse.data, null, 2)}`);

    await db.collection("requests").doc(request.id).delete();
  } catch (error) {
    console.error(`Failed to send training request for ${request.dni}:`, error);
    if (error.response) {
      console.error(`Status: ${error.response.status}`);
      console.error(`Data: ${JSON.stringify(error.response.data)}`);
      console.error(`Headers: ${JSON.stringify(error.response.headers)}`);
    }
  }
};

exports.sendTrainingRequests = functions.pubsub
    .schedule("every day 00:00")
    .onRun(async (context) => {
      const configDoc = await db
          .collection("config")
          .doc("trainingSchedule")
          .get();
      const config = configDoc.data();

      if (!config) {
        console.error("No schedule configuration found");
        return;
      }

      const today = new Date().getDay();
      const daysOfWeek = [
        "sunday",
        "monday",
        "tuesday",
        "wednesday",
        "thursday",
        "friday",
        "saturday",
      ];
      const todayString = daysOfWeek[today];

      if (!config[todayString]) {
        console.log(`Today is ${todayString}, 
          which is not a configured training day.`);
        return;
      }

      const snapshot = await db.collection("requests").get();
      const requests = snapshot.docs.map(
          (doc) => ({id: doc.id, ...doc.data()}),
      );

      for (let i = 0; i < requests.length; i += 5) {
        const batch = requests.slice(i, i + 5);
        await Promise.all(batch.map(processRequest));
        if (i + 5 < requests.length) {
          console.log("Waiting for 10 seconds before " +
            "processing the next batch...");
          await sleep(10000); // Wait for 10 seconds
        }
      }

      return null;
    });
