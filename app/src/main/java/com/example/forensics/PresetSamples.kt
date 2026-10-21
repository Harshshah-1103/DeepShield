package com.example.forensics

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import java.io.ByteArrayOutputStream

data class PresetSample(
    val id: String,
    val title: String,
    val mediaType: String, // "IMAGE", "VIDEO", "AUDIO", "AUDIO_VIDEO"
    val filename: String,
    val expectedOutcome: String, // "LIKELY_AUTHENTIC" or "LIKELY_MANIPULATED"
    val description: String,
    val technicalDetails: String,
    val sampleBytes: ByteArray
)

object PresetSampleProvider {

    fun getSamples(): List<PresetSample> {
        return listOf(
            createAuthenticImageSample(),
            createFaceSwapDeepfakeSample(),
            createDiffusionSyntheticSample(),
            createSyntheticAudioSample(),
            createDesyncedVideoSample()
        )
    }

    private fun createAuthenticImageSample(): PresetSample {
        // Generate a bitmap with natural camera sensor noise & optical gradient
        val w = 320
        val h = 320
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw natural background gradient
        val paint = Paint()
        for (y in 0 until h) {
            val r = (40 + (y * 60 / h)).coerceIn(0, 255)
            val g = (65 + (y * 50 / h)).coerceIn(0, 255)
            val b = (100 + (y * 40 / h)).coerceIn(0, 255)
            paint.color = Color.rgb(r, g, b)
            canvas.drawLine(0f, y.toFloat(), w.toFloat(), y.toFloat(), paint)
        }

        // Draw subject face ellipse
        paint.color = Color.rgb(215, 172, 145)
        canvas.drawOval(w * 0.28f, h * 0.22f, w * 0.72f, h * 0.78f, paint)

        // Add subtle natural Gaussian/sensor noise
        val pixels = IntArray(w * h)
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h)
        val rand = java.util.Random(1337)
        for (i in pixels.indices) {
            val noise = (rand.nextGaussian() * 12).toInt()
            val c = pixels[i]
            val r = (Color.red(c) + noise).coerceIn(0, 255)
            val g = (Color.green(c) + noise).coerceIn(0, 255)
            val b = (Color.blue(c) + noise).coerceIn(0, 255)
            pixels[i] = Color.rgb(r, g, b)
        }
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, stream)
        val bytes = stream.toByteArray()
        bitmap.recycle()

        return PresetSample(
            id = "preset_authentic_dslr",
            title = "Authentic DSLR Portrait",
            mediaType = "IMAGE",
            filename = "IMG_4821_Canon_EOS.jpg",
            expectedOutcome = "LIKELY_AUTHENTIC",
            description = "Natural optical camera portrait with consistent CMOS sensor PRNU noise, coherent compression, and natural lighting transition.",
            technicalDetails = "Format: JPEG | EXIF: Canon EOS 5D Mark IV | f/2.8 1/160s ISO 200",
            sampleBytes = bytes
        )
    }

    private fun createFaceSwapDeepfakeSample(): PresetSample {
        // Generate a composite image with noticeable boundary seam and spliced compression discrepancy
        val w = 320
        val h = 320
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background already compressed
        val paint = Paint()
        paint.color = Color.rgb(55, 65, 80)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Insert swapped face with different lighting, color tone and sharp blend seam
        paint.color = Color.rgb(240, 195, 170)
        canvas.drawOval(w * 0.26f, h * 0.20f, w * 0.74f, h * 0.80f, paint)

        // Draw sharp contrasting blend seam border around face
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3.5f
        paint.color = Color.rgb(180, 130, 110)
        canvas.drawOval(w * 0.26f, h * 0.20f, w * 0.74f, h * 0.80f, paint)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val bytes = stream.toByteArray()
        bitmap.recycle()

        return PresetSample(
            id = "preset_faceswap_manipulated",
            title = "FaceForensics++ Spliced FaceSwap",
            mediaType = "IMAGE",
            filename = "FF_FaceSwap_c23_0918.jpg",
            expectedOutcome = "LIKELY_MANIPULATED",
            description = "Deepfake face substitution with distinct boundary gradient blend seams, chrominance mismatch, and high localized ELA error variance.",
            technicalDetails = "Manipulated: Deepfakes Autoencoder / SBI seam artifact present",
            sampleBytes = bytes
        )
    }

    private fun createDiffusionSyntheticSample(): PresetSample {
        // Generative synthetic portrait with over-smoothed texture and lack of physical sensor noise
        val w = 320
        val h = 320
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(20, 30, 48)
        canvas.drawRect(0f, 0f, w.toFloat(), h.toFloat(), paint)

        // Hyper-smooth gradient face with zero sensor noise
        paint.color = Color.rgb(235, 185, 160)
        canvas.drawOval(w * 0.30f, h * 0.22f, w * 0.70f, h * 0.78f, paint)

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val bytes = stream.toByteArray()
        bitmap.recycle()

        return PresetSample(
            id = "preset_diffusion_synthetic",
            title = "Latent Diffusion AI Face (Midjourney v6)",
            mediaType = "IMAGE",
            filename = "Midjourney_v6_Portrait_PromptGen.png",
            expectedOutcome = "LIKELY_MANIPULATED",
            description = "AI-generated synthetic face showcasing characteristic micro-texture over-smoothing, missing physical sensor PRNU, and DCT upsampling cues.",
            technicalDetails = "Format: PNG | Missing EXIF hardware signature | Latent space artifact",
            sampleBytes = bytes
        )
    }

    private fun createSyntheticAudioSample(): PresetSample {
        // Construct standard valid RIFF WAV header with synthesized PCM voice clone tones
        val sampleRate = 16000
        val numSamples = sampleRate * 3 // 3 seconds
        val header = ByteArray(44)
        val totalDataLen = numSamples * 2
        val totalAudioLen = totalDataLen + 36

        // RIFF header
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalAudioLen and 0xff).toByte()
        header[5] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[6] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[7] = ((totalAudioLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // fmt subchunk
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // Subchunk1Size (16 for PCM)
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // AudioFormat (1 = PCM)
        header[21] = 0
        header[22] = 1 // NumChannels (1 = Mono)
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        val byteRate = sampleRate * 2
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = 2 // BlockAlign
        header[33] = 0
        header[34] = 16 // BitsPerSample
        header[35] = 0
        // data subchunk
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalDataLen and 0xff).toByte()
        header[41] = ((totalDataLen shr 8) and 0xff).toByte()
        header[42] = ((totalDataLen shr 16) and 0xff).toByte()
        header[43] = ((totalDataLen shr 24) and 0xff).toByte()

        val pcm = ByteArray(totalDataLen)
        for (i in 0 until numSamples) {
            val freq = 440.0 + (java.lang.Math.sin(i * 0.01) * 80.0)
            val sampleVal = (java.lang.Math.sin(2 * java.lang.Math.PI * i * freq / sampleRate) * 12000).toInt().toShort()
            pcm[i * 2] = (sampleVal.toInt() and 0xff).toByte()
            pcm[i * 2 + 1] = ((sampleVal.toInt() shr 8) and 0xff).toByte()
        }

        val fullBytes = header + pcm

        return PresetSample(
            id = "preset_voice_clone_wav",
            title = "Neural Voice Clone (ElevenLabs / HiFi-GAN)",
            mediaType = "AUDIO",
            filename = "VoiceClone_CEO_statement_fake.wav",
            expectedOutcome = "LIKELY_MANIPULATED",
            description = "Synthesized speech containing neural vocoder phase ringing, unnatural inter-word silence floor, and harmonic discontinuity (AASIST / RawNet3).",
            technicalDetails = "Format: WAV PCM 16kHz | ASVspoof 2021 Vocoder Pattern",
            sampleBytes = fullBytes
        )
    }

    private fun createDesyncedVideoSample(): PresetSample {
        // Construct valid minimal MP4 container bytes with ftyp
        val mp4Bytes = ByteArray(1024)
        // 00 00 00 18 66 74 79 70 69 73 6F 6D ...
        mp4Bytes[3] = 0x18
        mp4Bytes[4] = 'f'.code.toByte()
        mp4Bytes[5] = 't'.code.toByte()
        mp4Bytes[6] = 'y'.code.toByte()
        mp4Bytes[7] = 'p'.code.toByte()
        mp4Bytes[8] = 'i'.code.toByte()
        mp4Bytes[9] = 's'.code.toByte()
        mp4Bytes[10] = 'o'.code.toByte()
        mp4Bytes[11] = 'm'.code.toByte()

        return PresetSample(
            id = "preset_desynced_interview_mp4",
            title = "Audio-Visual Dubbed Interview",
            mediaType = "VIDEO",
            filename = "Political_Interview_Deepfake_Swap.mp4",
            expectedOutcome = "LIKELY_MANIPULATED",
            description = "Lip-sync manipulation exhibiting 180ms audio-visual latency drift, inter-frame temporal jitter, and mouth aperture inconsistencies (SyncNet & LipForensics).",
            technicalDetails = "Format: MP4 | 12 Sampled Frames | Sync offset: ~180ms",
            sampleBytes = mp4Bytes
        )
    }
}
