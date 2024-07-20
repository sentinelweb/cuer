package uk.co.sentinelweb.cuer.hub.di

import PlatformWifiInfo
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.russhwolf.settings.JvmPreferencesSettings
import com.russhwolf.settings.Settings
import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.db.repository.file.AFile
import uk.co.sentinelweb.cuer.app.db.repository.file.AssetOperations
import uk.co.sentinelweb.cuer.app.db.repository.file.ConfigDirectory
import uk.co.sentinelweb.cuer.app.db.repository.file.JsonFileInteractor
import uk.co.sentinelweb.cuer.app.di.SharedAppModule
import uk.co.sentinelweb.cuer.app.service.remote.RemoteServerContract
import uk.co.sentinelweb.cuer.app.ui.common.resources.DefaultStringDecoder
import uk.co.sentinelweb.cuer.app.ui.common.resources.StringDecoder
import uk.co.sentinelweb.cuer.app.ui.player.PlayerContract
import uk.co.sentinelweb.cuer.app.ui.ytplayer.floating.FloatingPlayerContract
import uk.co.sentinelweb.cuer.app.util.chromecast.listener.ChromeCastPlayerContextHolder
import uk.co.sentinelweb.cuer.app.util.permission.LocationPermissionLaunch
import uk.co.sentinelweb.cuer.app.util.share.scan.LinkScanner
import uk.co.sentinelweb.cuer.app.util.wrapper.VibrateWrapper
import uk.co.sentinelweb.cuer.core.wrapper.ConnectivityWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.SystemLogWrapper
import uk.co.sentinelweb.cuer.core.wrapper.WifiStateProvider
import uk.co.sentinelweb.cuer.db.di.DatabaseCommonModule
import uk.co.sentinelweb.cuer.db.di.JvmDatabaseModule
import uk.co.sentinelweb.cuer.di.DomainModule
import uk.co.sentinelweb.cuer.di.JvmDomainModule
import uk.co.sentinelweb.cuer.domain.BuildConfigDomain
import uk.co.sentinelweb.cuer.hub.BuildConfigInject
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerService
import uk.co.sentinelweb.cuer.hub.service.remote.RemoteServerServiceManager
import uk.co.sentinelweb.cuer.hub.service.update.UpdateService
import uk.co.sentinelweb.cuer.hub.ui.filebrowser.FilesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.home.HomeUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.local.LocalUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.player.EmptyChromeCastPlayerContextHolder
import uk.co.sentinelweb.cuer.hub.ui.player.EmptyFloatingPlayerManager
import uk.co.sentinelweb.cuer.hub.ui.player.EmptyPlayerControls
import uk.co.sentinelweb.cuer.hub.ui.player.vlc.VlcPlayerUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.preferences.PreferencesUiCoordinator
import uk.co.sentinelweb.cuer.hub.ui.remotes.RemotesUiCoordinator
import uk.co.sentinelweb.cuer.hub.util.permission.EmptyLocationPermissionLaunch
import uk.co.sentinelweb.cuer.hub.util.platform.getNodeDeviceType
import uk.co.sentinelweb.cuer.hub.util.platform.getOSData
import uk.co.sentinelweb.cuer.hub.util.remote.EmptyWakeLockManager
import uk.co.sentinelweb.cuer.hub.util.remote.FileEncryption
import uk.co.sentinelweb.cuer.hub.util.remote.KeyStoreManager
import uk.co.sentinelweb.cuer.hub.util.remote.RemoteConfigFileInitialiseer
import uk.co.sentinelweb.cuer.hub.util.share.scan.TodoLinkScanner
import uk.co.sentinelweb.cuer.hub.util.wrapper.EmptyVibrateWrapper
import uk.co.sentinelweb.cuer.net.ApiKeyProvider
import uk.co.sentinelweb.cuer.net.NetModuleConfig
import uk.co.sentinelweb.cuer.net.client.ServiceType
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckManager
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityCheckTimer
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityChecker
import uk.co.sentinelweb.cuer.net.connectivity.ConnectivityMonitor
import uk.co.sentinelweb.cuer.net.di.NetModule
import uk.co.sentinelweb.cuer.net.key.CuerPixabayApiKeyProvider
import uk.co.sentinelweb.cuer.net.key.CuerYoutubeApiKeyProvider
import uk.co.sentinelweb.cuer.net.wifi.DesktopConnectivityWrapper
import uk.co.sentinelweb.cuer.net.wifi.PlatformWifiStateProvider
import uk.co.sentinelweb.cuer.remote.server.LocalRepository
import uk.co.sentinelweb.cuer.remote.server.RemotesRepository
import uk.co.sentinelweb.cuer.remote.server.WakeLockManager
import uk.co.sentinelweb.cuer.remote.server.di.RemoteModule
import java.io.File
import java.util.prefs.Preferences


object Modules {

    private val scopedModules = listOf(
        HomeUiCoordinator.uiModule,
        RemotesUiCoordinator.uiModule,
        RemoteServerService.serviceModule,
        UpdateService.serviceModule,
        LocalUiCoordinator.uiModule,
        PreferencesUiCoordinator.uiModule,
        FilesUiCoordinator.uiModule,
        VlcPlayerUiCoordinator.uiModule,
    )

    private val resourcesModule = module {
        factory<StringDecoder> { DefaultStringDecoder() }
        factory { AssetOperations() }
    }

    private val utilModule = module {
        factory<LogWrapper> { SystemLogWrapper() }
        factory { LifecycleRegistry() }
        factory<LocationPermissionLaunch> { EmptyLocationPermissionLaunch() }
        factory<VibrateWrapper> { EmptyVibrateWrapper() }
        factory<Settings> {
            val preferences = Preferences.userRoot().node(".cuer")
            JvmPreferencesSettings(preferences)
        }
    }

    private val connectivityModule = module {
        single<ConnectivityWrapper> { DesktopConnectivityWrapper(get(), get(), get(), get()) }
//        single<WifiStateProvider> { DesktopWifiStateProvider(get(), get(), get(), get()) }
        single<WifiStateProvider> { PlatformWifiStateProvider(get(), get(), get()) }
        single { PlatformWifiInfo() }
        single { ConnectivityCheckManager(get(), get(), get()) }
        single { ConnectivityMonitor(get(), get(), get()) }
        single { ConnectivityCheckTimer() }
        single { ConnectivityChecker() }
    }

    private val configModule = module {
        single {
            // todo make SharedAppDependencies object : see ios di
            BuildConfigDomain(
                isDebug = BuildConfigInject.isDebug,
                cuerRemoteEnabled = BuildConfigInject.cuerRemoteEnabled,
                versionCode = BuildConfigInject.versionCode,
                version = BuildConfigInject.version,
                device = getOSData(),
                deviceType = getNodeDeviceType()
            )
        }
        factory {
            // todo use SharedAppDependencies object : see ios di
            NetModuleConfig(debug = true)
        }
        factory<ApiKeyProvider>(named(ServiceType.YOUTUBE)) { CuerYoutubeApiKeyProvider() }
        factory<ApiKeyProvider>(named(ServiceType.PIXABAY)) { CuerPixabayApiKeyProvider() }
    }

    private val remoteModule = module {
        single { RemoteConfigFileInitialiseer() }
        single { FileEncryption(get()) }
        single { KeyStoreManager() }
        single {
            "localNode.json"
                .let { AFile(File(ConfigDirectory.Path, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { LocalRepository(it, get(), get(), get(), get()) }
        }
        single {
            "remoteNodes.json"
                .let { AFile(File(ConfigDirectory.Path, it).absolutePath) }
                .let { JsonFileInteractor(it, get()) }
                .let { RemotesRepository(it, get(), get(), get()) }
        }
        factory<WakeLockManager> { EmptyWakeLockManager() }
        factory<LinkScanner> { TodoLinkScanner() }
        factory<RemoteServerContract.Service> { RemoteServerService(get()) }
        single<RemoteServerContract.Manager> { RemoteServerServiceManager(get()) }
        factory<PlayerContract.PlayerControls> { EmptyPlayerControls() }
        factory<ChromeCastPlayerContextHolder> { EmptyChromeCastPlayerContextHolder() }
        factory<FloatingPlayerContract.Manager> { EmptyFloatingPlayerManager() }
    }

    val allModules = listOf(resourcesModule)
        .plus(utilModule)
        .plus(remoteModule)
        .plus(configModule)
        .plus(connectivityModule)
        .plus(scopedModules)
        .plus(DomainModule.allModules)
        .plus(JvmDomainModule.allModules)
        .plus(RemoteModule.objectModule)
        .plus(SharedAppModule.modules)
        .plus(NetModule.modules)
        .plus(JvmDatabaseModule.modules)
        .plus(DatabaseCommonModule.modules)
}