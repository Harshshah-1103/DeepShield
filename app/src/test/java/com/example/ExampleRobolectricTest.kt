package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.forensics.PresetSampleProvider
import com.example.forensics.SecureIngestion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("DeepShield AI", appName)
    }

    @Test
    fun `test secure ingestion on valid preset samples`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val samples = PresetSampleProvider.getSamples()
        assertTrue("Preset samples must not be empty", samples.isNotEmpty())

        val imageSample = samples.first { it.mediaType == "IMAGE" }
        val ingestion = SecureIngestion.ingestBytes(context, imageSample.sampleBytes, imageSample.filename)

        assertTrue("Ingestion must succeed", ingestion.success)
        assertEquals("IMAGE", ingestion.mediaType)
        assertEquals("JPEG", ingestion.format)
        assertTrue("SHA-256 hash must be 64 characters", ingestion.sha256Hash.length == 64)
        assertNotNull("Decoded working bitmap must be available", ingestion.bitmap)
    }

    @Test
    fun `test secure ingestion rejects empty bytes`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val ingestion = SecureIngestion.ingestBytes(context, ByteArray(0), "empty.jpg")
        assertTrue("Empty bytes must fail ingestion", !ingestion.success)
    }

    @Test
    fun `test MainActivity launches successfully`() {
        val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        assertNotNull("Activity should launch and not be null", activity)
    }
}
