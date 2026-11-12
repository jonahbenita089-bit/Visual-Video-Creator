package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("Video Creator", appName)
  }

  @Test
  fun `template repository contains dancing dog and diverse templates`() {
    val templates = com.example.data.TemplateRepository.templates
    org.junit.Assert.assertTrue(templates.isNotEmpty())
    val dancingDog = templates.firstOrNull { it.id == "social_dancing_dog" }
    org.junit.Assert.assertNotNull(dancingDog)
    assertEquals(4, dancingDog?.scenes?.size)
  }

  @Test
  fun `video limits support 700 scenes and 30 seconds duration`() {
    assertEquals(700, com.example.model.VideoLimits.MAX_SCENES)
    assertEquals(30.0f, com.example.model.VideoLimits.MAX_SCENE_DURATION_SECONDS)
    assertEquals(1.0f, com.example.model.VideoLimits.MIN_SCENE_DURATION_SECONDS)
  }
}
