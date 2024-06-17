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

      const logsCollection = admin.firestore().collection("logs");

      if (userCategoria) {
        try {
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
                                        logEventType: "INFO",
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
        } catch (e){
            console.log(
                "ERROR CON USUARIO - User:",
                userId,
                e
              );
        }
      } else {
        console.log("ERROR - User:", userId, "NO TIENE CATEGORIA.");
      }

      const suspendidoDesde = userData.suspendidoDesde;
      const suspendidoHasta = userData.suspendidoHasta;

      if (suspendidoDesde && suspendidoHasta){
        try {
            const suspendidoDesdeDate = suspendidoDesde.toDate();
            const suspendidoHastaDate = suspendidoHasta.toDate();
            if (currentDate >= suspendidoDesdeDate && currentDate <= suspendidoHastaDate){
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
            } else if (currentDate > suspendidoHastaDate){
                if (currentUserEstado == "Inactivo"){
                    usuariosCollection.doc(userId).update({estado: "Activo"});
                    console.log(
                        "User:",
                        userId,
                        "HA SIDO HABILITADO DEBIDO A QUE SU SUSPENSION HA TERMINADO"
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
                    usuariosCollection.doc(userId).update({
                        suspendidoDesde: admin.firestore.FieldValue.delete(),
                        suspendidoHasta: admin.firestore.FieldValue.delete(),
                    });
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
