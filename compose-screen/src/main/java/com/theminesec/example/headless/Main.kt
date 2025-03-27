package com.theminesec.example.headless

import com.theminesec.example.headless.landing.LandingMain

class Main : LandingMain() {
    override val headlessImplClass
        get() = HeadlessImplWithScreenProvider::class.java
}
