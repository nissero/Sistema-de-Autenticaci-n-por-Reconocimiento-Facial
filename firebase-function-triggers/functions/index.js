// The Cloud Functions for Firebase SDK to set up triggers and logging.
const {onSchedule} = require("firebase-functions/v2/scheduler");
const {logger} = require("firebase-functions");
const functions = require("firebase-functions");
const nodemailer = require('nodemailer');
const moment = require('moment-timezone');

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

const gmailEmail = functions.config().email.user;
const gmailPassword = functions.config().email.pass;

const transporter = nodemailer.createTransport({
    service: "gmail", // Puedes usar otros servicios de correo
    auth: {
        user: gmailEmail,
        pass: gmailPassword // Es recomendable usar variables de entorno para seguridad
    }
});

exports.scheduledEmail = functions.pubsub.schedule("0 23 * * *").timeZone("America/Argentina/Buenos_Aires").onRun(async (context) => {
    const db = admin.firestore();
    const doc = await db.collection('data jerarquico').doc('info').get();
    const recipientEmail = doc.data().mail;

    const docLogs = db.collection('logs');

    let emailContent = 'RESUMEN DIARIO DE AUTENTICACIONES FALLIDAS: \n';

    const currentDate = new Date();
    const startOfDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), 0, 0, 0);
    const endOfDay = new Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate(), 23, 59, 59);

    const logs = await docLogs.where('timestamp', '>=', startOfDay)
        .where('timestamp', '<=', endOfDay)
        .where('logEventName', 'in', ['USER UNSUCCESSFUL AUTHENTICATION', 'RRHH UNSUCCESSFUL LOGIN', 'SECURITY UNSUCCESSFUL LOGIN', 'ADMIN UNSUCCESSFUL LOGIN', 'HIERARCHICAL UNSUCCESSFUL LOGIN']).get();

    for (const logDoc of logs.docs) {
        const logData = logDoc.data();
        const timestampUTC3 = moment(logData.timestamp.toDate()).tz('America/Argentina/Buenos_Aires').format('YYYY-MM-DD HH:mm:ss');
        emailContent += `Categoría: ${logData.category} | DNI Usuario Maestro: ${logData.dniMasterUser} | DNI Usuario Afectado: ${logData.dniUserAffected} | Evento: ${logData.logEventName} | Tipo de Evento: ${logData.logEventType} | Fecha y Hora: ${timestampUTC3}\n \n`;
    }

    const formattedDate = currentDate.toLocaleDateString('es-AR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric'
    });

    const mailOptions = {
        from: gmailEmail,
        to: recipientEmail.toString(),
        subject: `RESUMEN DIARIO DE AUTENTICACIONES FALLIDAS - ${formattedDate}`,
        text: emailContent
    };

    return transporter.sendMail(mailOptions, (error, info) => {
        if (error) {
            return console.log(error.toString());
        }
        console.log("Email sent: " + info.response);
    });
});