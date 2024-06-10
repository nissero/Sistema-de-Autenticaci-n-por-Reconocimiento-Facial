package com.biogin.myapplication.utils

import java.util.Calendar
import java.util.Properties
import javax.mail.Address
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart


class EmailService(private var server: String, private var port: Int) {

    data class Email(
        val auth: Authenticator,
        val toList: List<InternetAddress>,
        val from: Address,
        val subject: String,
        val body: String
    )

    class UserPassAuthenticator(private val username: String, private val password: String) : Authenticator() {
        override fun getPasswordAuthentication(): PasswordAuthentication {
            return PasswordAuthentication(username, password)
        }
    }

    fun send(email: Email) {
        val props = Properties()
        props["mail.smtp.auth"] = "true"
        props["mail.user"] = email.from
        props["mail.smtp.host"] = server
        props["mail.smtp.port"] = port
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.ssl.trust"] = server
        props["mail.mime.charset"] = "UTF-8"
        val msg: Message = MimeMessage(Session.getDefaultInstance(props, email.auth))
        msg.setFrom(email.from)
        msg.sentDate = Calendar.getInstance().time
        msg.setRecipients(Message.RecipientType.TO, email.toList.toTypedArray())
        msg.replyTo = arrayOf(email.from)

        msg.addHeader("X-Mailer", CLIENT_NAME)
        msg.addHeader("Precedence", "bulk")
        msg.subject = email.subject

        msg.setContent(MimeMultipart().apply {
            addBodyPart(MimeBodyPart().apply {
                setText(email.body, "iso-8859-1")
            })
        })
        Transport.send(msg)
    }

    companion object {
        const val CLIENT_NAME = "Android StackOverflow programmatic email"
    }
}