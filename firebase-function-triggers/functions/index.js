// The Cloud Functions for Firebase SDK to set up triggers and logging.
const {onSchedule} = require("firebase-functions/v2/scheduler");
const {logger} = require("firebase-functions");

// The Firebase Admin SDK to access Firestore (if needed).
const admin = require("firebase-admin");
admin.initializeApp();

// Function to be executed every day at midnight
// Cambiar a 24 hours para que corra cada día, ahora esta corriendo cada min
exports.changeUsersEstado = onSchedule("every day 00:00", async (context) => {
  console.log("Cloud Function triggered at:", context.timestamp);

  const db = admin.firestore();
  const usuariosCollection = db.collection("usuarios");
  const categoriasCollection = db.collection("categorias");

  usuariosCollection.get().then((querySnapshot) => {
    querySnapshot.forEach((usuario) => {
      const userId = usuario.id;
      const userData = usuario.data();
      const userCategoria = userData.categoria;
      const currentUserEstado = userData.estado;

      const currentDate = new Date();

      console.log("Ejecutando usuario: ", userId)

      const logsCollection = admin.firestore().collection("logs");

      if (userCategoria) {
        categoriasCollection.doc(userCategoria).get().then((categoriaDoc) => {
            if (categoriaDoc.exists) {
                const categoriaData = categoriaDoc.data();
                if (categoriaData && categoriaData.temporal) {
                    const trabajaDesdeTimestamp = userData.trabajaDesde;
                    const trabajaHastaTimestamp = userData.trabajaHasta;

                    if (userData.trabajaDesde && userData.trabajaHasta){

                        const trabajaDesdeDate = trabajaDesdeTimestamp.toDate();
                        const trabajaHastaDate = trabajaHastaTimestamp.toDate();
                        
                        if (currentDate >= trabajaDesdeDate && currentDate <= trabajaHastaDate){
                            console.log(
                                "User:",
                                userId,
                                "EL USUARIO ESTA HABILITADO A TRABAJAR EN EL DIA DE LA FECHA"
                              );
                            if (currentUserEstado == "Inactivo"){
                                usuariosCollection.doc(userId).update({ estado: "Activo" });
                                console.log(
                                    "User:",
                                    userId,
                                    "USUARIO HA SIDO HABILITADO PARA TRABAJAR EXTERNAMENTE"
                                  );

                                const newLog = {
                                    category: userCategoria,
                                    dniMasterUser: "",
                                    dniUserAffected: userId,
                                    logEventName: "USER ACTIVATION",
                                    logEventType: "INFO",
                                    timestamp: admin.firestore.FieldValue.serverTimestamp()
                                };

                                logsCollection.add(newLog);
                            }
                        } else {
                            console.log(
                                "User:",
                                userId,
                                "EL USUARIO NO ESTA HABILITADO A TRABAJAR"
                              );
                              if (currentUserEstado == "Activo"){
                                usuariosCollection.doc(userId).update({ estado: "Inactivo" });
                                console.log(
                                    "User:",
                                    userId,
                                    "USUARIO HA SIDO DESHABILITADO PARA TRABAJAR EXTERNAMENTE"
                                  );

                                const newLog = {
                                    category: userCategoria,
                                    dniMasterUser: "",
                                    dniUserAffected: userId,
                                    logEventName: "USER INACTIVATION",
                                    logEventType: "INFO,
                                    timestamp: admin.firestore.FieldValue.serverTimestamp()
                                };

                                logsCollection.add(newLog);
                            }
                        }
                    }
                }
            }
        }).catch((error) => {
            console.error("ERROR FETCHING CATEGORIA ARGUMENT", error);
        });
      } else {
        console.log("ERROR - User:", userId, "NO TIENE CATEGORIA.");
      }

      const suspendidoDesde = userData.suspendidoDesde;
      const suspendidoHasta = userData.suspendidoHasta;

      if (suspendidoDesde && suspendidoHasta){
        try {
            const suspendidoDesdeDate = suspendidoDesde.toDate();
            const suspendidoHastaDate = suspendidoHasta.toDate();

            console.log(
                "User:",
                userId,
                "TIENE FECHA DE SUSPENSION",
                suspendidoDesdeDate,
                suspendidoHastaDate
              );
    
            if (currentDate >= suspendidoDesdeDate && currentDate <= suspendidoHastaDate){
                console.log(
                    "User:",
                    userId,
                    "EL DIA DE LA FECHA ESTA DENTRO DEL PERIODO DE SUSPENSION"
                  );
                if (currentUserEstado == "Activo"){
                    usuariosCollection.doc(userId).update({ estado: "Inactivo" });
                    console.log(
                        "User:",
                        userId,
                        "USUARIO HA SIDO DESHABILITADO YA QUE EL DIA DE LA FECHA ESTA DENTRO DEL PERIODO DE SUSPENSION"
                      );
                    
                    const newLog = {
                        category: userCategoria,
                        dniMasterUser: "",
                        dniUserAffected: userId,
                        logEventName: "USER INACTIVATION",
                        logEventType: "INFO",
                        timestamp: admin.firestore.FieldValue.serverTimestamp()
                    };

                    logsCollection.add(newLog);
                }
            } else {
                console.log(
                    "User:",
                    userId,
                    "EL PERIODO DE SUSPENSION HA TERMINADO/EL DIA ACTUAL ESTA FUERA DEL PERIODO DE SUSPENSION"
                  );
                if (currentUserEstado == "Inactivo"){
                    usuariosCollection.doc(userId).update({ estado: "Activo" });
                    console.log(
                        "User:",
                        userId,
                        "USUARIO HA SIDO HABILITADO YA QUE EL PERIODO DE SUSPENSION HA TERMINADO"
                      );

                    const newLog = {
                        category: userCategoria,
                        dniMasterUser: "",
                        dniUserAffected: userId,
                        logEventName: "USER ACTIVATION",
                        logEventType: "INFO",
                        timestamp: admin.firestore.FieldValue.serverTimestamp()
                    };

                    logsCollection.add(newLog);
                }
            }
        } catch (e){
            console.log(
                "ERROR CON USUARIO - User:",
                userId,
                e
              );
        }
      }

    });

    logger.log("Daily scheduled function executed successfully!");
  });
});
