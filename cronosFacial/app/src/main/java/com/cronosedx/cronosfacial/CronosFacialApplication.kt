package com.cronosedx.cronosfacial

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Cronos Facial Recognition App.
 * 
 * This class is annotated with @HiltAndroidApp to enable Hilt dependency injection
 * throughout the application.
 */
@HiltAndroidApp
class CronosFacialApplication : Application()
