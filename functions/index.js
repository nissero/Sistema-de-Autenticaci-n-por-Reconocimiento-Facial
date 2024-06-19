const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");
const FormData = require("form-data");
const {SecretManagerServiceClient} = require("@google-cloud/secret-manager");

// Create a Secret Manager client
const client = new SecretManagerServiceClient();

/**
 * Retrieves the secret version from Secret Manager.
 * @param {string} name The name of the secret version.
 * @return {object} The parsed secret payload.
 */
async function accessSecretVersion(name) {
  const [version] = await client.accessSecretVersion({name});
  const payload = version.payload.data.toString("utf8");
  return JSON.parse(payload);
}

/**
 * Initializes Firebase Admin SDK, Firestore, and Storage Bucket.
 * @return {object} An object containing Firestore and Storage Bucket instances.
 */
async function initializeFirebase() {
  const secretName = "projects/620958752168/secrets" +
  "/FirebaseServiceAccount/versions/2";
  const secretValue = await accessSecretVersion(secretName);
  admin.initializeApp({
    credential: admin.credential.cert(secretValue),
    storageBucket: "sistema-autenticacion-facial.appspot.com",
  });
  const db = admin.firestore();
  const bucket = admin.storage().bucket();
  return {db, bucket};
}

// Initialize Firebase Admin SDK, Firestore, and Storage Bucket
const firebaseInitializationPromise = initializeFirebase();

exports.sendTrainingRequests = functions.pubsub
    .schedule("every day 00:00")
    .onRun(async (context) => {
      console.log("Function execution started with context:", context);
      try {
        const {db, bucket} = await firebaseInitializationPromise;

        if (!db) {
          throw new Error("Firestore is not initialized");
        }

        const configDoc = await db.collection("config")
            .doc("trainingSchedule").get();
        const config = configDoc.data();

        console.log("Config data:", config);

        if (!config) {
          console.error("No schedule configuration found");
          return;
        }

        const today = new Date().getDay();
        const daysOfWeek = ["sunday", "monday", "tuesday",
          "wednesday", "thursday", "friday", "saturday"];
        const todayString = daysOfWeek[today];

        if (!config[todayString]) {
          console.log(`Today is ${todayString},
             which is not a configured training day.`);
          return;
        }

        const snapshot = await db.collection("requests")
            .where("processed", "==", false).get();
        const requests = snapshot.docs
            .map((doc) => ({id: doc.id, ...doc.data()}));

        for (let i = 0; i < requests.length; i += 5) {
          const batch = requests.slice(i, i + 5);
          await Promise.all(batch.map(
              (request) => processRequest(request, db, bucket)));
          if (i + 5 < requests.length) {
            console.log("Waiting for 10 seconds before" +
              " processing the next batch...");
            await sleep(10000);
          }
        }
      } catch (error) {
        console.error("Error processing requests:", error);
      }

      return null;
    });

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Processes the training request.
 * @param {object} request The training request.
 * @param {object} db Firestore database instance.
 * @param {object} bucket Storage bucket instance.
 */
async function processRequest(request, db, bucket) {
  try {
    if (!Array.isArray(request.photoPaths) || request.photoPaths.length === 0) {
      console.error(`Request for ${request.dni} has no valid photoPaths.`);
      return; // Skip this request
    }

    const imagePromises = request.photoPaths.map(async (path) => {
      const file = bucket.file(path);
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

    // Mark the request as processed
    await db.collection("requests").doc(request.id).update({processed: true});
  } catch (error) {
    console.error(`Failed to send training request for ${request.dni}:`, error);
    if (error.response) {
      console.error(`Status: ${error.response.status}`);
      console.error(`Data: ${JSON.stringify(error.response.data)}`);
      console.error(`Headers: ${error.response.headers}`);
    }
  }
}
