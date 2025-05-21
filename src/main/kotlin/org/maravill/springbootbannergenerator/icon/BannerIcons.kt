package org.maravill.springbootbannergenerator.icon

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object BannerIcons {
    val bannerIcon: Icon = IconLoader.getIcon("/icons/bannerIcon.svg", BannerIcons::class.java)
}