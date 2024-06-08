const functions = require("firebase-functions");
const admin = require("firebase-admin");
const axios = require("axios");

admin.initializeApp();

const db = admin.firestore();

exports.sendTrainingRequests = functions.pubsub.schedule("every saturday 00:00")
    .onRun(async (context) => {
      const configDoc = await db.
          collection("config").doc("trainingSchedule").get();
      const config = configDoc.data();

      if (!config) {
        console.error("No schedule configuration found");
        return;
      }

      const today = new Date()
          .toLocaleString("en-us", {weekday: "long"}).toLowerCase();

      if (!config[today]) {
        console.log(`Today is ${today},
             which is not a configured training day.`);
        return;
      }

      const snapshot = await db.collection("requests").get();
      const requests = snapshot.docs
          .map((doc) => ({id: doc.id, ...doc.data()}));

      for (const request of requests) {
        try {
          const response = await axios.post("https://Biogin.pythonanywhere.com/train", {
            image: request.image,
            name: request.dni,
          });

          console.log(`Training result for ${request.dni}: ${response.data}`);

          // Delete the request after successful processing
          await db.collection("requests").doc(request.id).delete();
        } catch (error) {
          console.error(`Failed to send training
             request for ${request.dni}:`, error);
        }
      }

      return null;
    });
