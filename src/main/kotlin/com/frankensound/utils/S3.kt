package com.frankensound.utils

import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.DeleteObjectRequest
import aws.sdk.kotlin.services.s3.model.GetObjectRequest
import aws.sdk.kotlin.services.s3.model.ListObjectsRequest
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import aws.smithy.kotlin.runtime.content.toByteArray

val s3Client = S3Client {
    region = System.getenv("REGION")
    credentialsProvider = EnvironmentCredentialsProvider()
}

suspend fun putS3Object(bucketName: String, objectKey: String, fileBytes: ByteArray) {
    val metadataVal = mutableMapOf<String, String>()
    metadataVal["myVal"] = "test"

    val request = PutObjectRequest {
        bucket = bucketName
        key = objectKey
        metadata = metadataVal
        body = ByteStream.fromBytes(fileBytes)
    }

    try {
        val response = s3Client.putObject(request)
        println("Tag information is ${response.eTag}")
    } catch (e: Exception) {
        println("Error putting object to S3: ${e.message}")
    }
}

suspend fun getObject(bucketName: String, keyName: String, rangeValue: String?): ResponseS3? {
    val request = GetObjectRequest {
        key = keyName
        bucket = bucketName
        range = rangeValue
    }

    return s3Client.getObject(request) { resp ->
        if (resp.body == null) {
            null
        } else {
            ResponseS3(resp.body!!.toByteArray(), resp.contentRange, resp.acceptRanges, resp.body!!.contentLength)
        }
    }
}

suspend fun listBucketObs(bucketName: String): ArrayList<String> {
    val list = ArrayList<String>()
    val request = ListObjectsRequest {
        bucket = bucketName
    }
    val response = s3Client.listObjects(request)
    response.contents?.forEach { myObject ->
        list.add(myObject.key.toString())
    }
    return list
}

suspend fun deleteS3Object(bucketName: String, objectKey: String) {
    val request = DeleteObjectRequest {
        bucket = bucketName
        key = objectKey
    }

    s3Client.deleteObject(request)
}

data class ResponseS3(
    val data: ByteArray,
    val contentRange: String?,
    val acceptRanges: String?,
    val contentLength: Long?,
)