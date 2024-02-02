package com.example.plugins

import com.cloudinary.Cloudinary
import com.example.Image
import com.mongodb.ConnectionString
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.apache.commons.logging.Log
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import java.util.Base64


fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
            print("Server Running.....")
        }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }

    val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "dblanaloq", "api_key" to "642241265413364", "api_secret" to "UNRitpZxCwAJoqCXiTT3vxAIcM4"
        )
    )

    val client = KMongo.createClient(
        ConnectionString("mongodb+srv://eslamahmed:25317768@cluster1.5rruj5l.mongodb.net/")
    ).coroutine
    val database = client.getDatabase("ktor-api")
//async { database.createCollection("images") }
    val images = database.getCollection<Image>("images")

    routing {
        route("/api/images") {
            post {
                try {
                    val multipartData: List<PartData> = (call.receiveMultipart() as MultiPartData).readAllParts()

                    if (multipartData.isNotEmpty()) {
                        for (filePart in multipartData) {
                            if (filePart is PartData.FileItem) {
                                val imageData = filePart.streamProvider().readBytes()
                                val uploadResult = cloudinary.uploader().upload(imageData, null)

                                // Optionally, save Cloudinary URL or other details to your database
                                val imageUrl = uploadResult["secure_url"] as? String
                                println(imageUrl)
                                call.respondText("Image uploaded to Cloudinary: $imageUrl", status = HttpStatusCode.OK)
                            }
                        }
                    } else {
                        call.respondText("No file received", status = HttpStatusCode.BadRequest)
                    }
                } catch (e: ContentTransformationException) {
                    call.respondText("Invalid file format", status = HttpStatusCode.BadRequest)
                }
            }
        }

    }
}

suspend fun getImagesUrl() {

    val url = "https://console.cloudinary.com/pm/c-e561266127ec563ee00192a0f1d36c/media-explorer/api/animy"
    val client = HttpClient()

    try {
        val response=client.get(url){
            headers.append("Authorization","Basic"+ b64encode("642241265413364:UNRitpZxCwAJoqCXiTT3vxAIcM4"))
        }
    } catch (e: Exception) {
        println("Error : ${e.localizedMessage}")
    } finally {
        client.close()
    }
}

fun b64encode(value: String): String {
    return Base64.getEncoder().encodeToString(value.toByteArray())
}

