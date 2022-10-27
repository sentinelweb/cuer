package uk.co.sentinelweb.cuer.app.di

import org.koin.core.qualifier.named
import org.koin.dsl.module
import uk.co.sentinelweb.cuer.app.backup.BackupCheck
import uk.co.sentinelweb.cuer.app.backup.version.ParserFactory
import uk.co.sentinelweb.cuer.app.db.repository.file.PlatformFileOperation
import uk.co.sentinelweb.cuer.app.orchestrator.*
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.LOCAL_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.NEWITEMS_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.RECENT_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.STAR_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.UNFINISHED_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.PlaylistMemoryRepository.Companion.YOUTUBE_SEARCH_PLAYLIST
import uk.co.sentinelweb.cuer.app.orchestrator.memory.interactor.*
import uk.co.sentinelweb.cuer.app.orchestrator.util.*
import uk.co.sentinelweb.cuer.app.queue.QueueMediator
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorContract
import uk.co.sentinelweb.cuer.app.queue.QueueMediatorState
import uk.co.sentinelweb.cuer.app.ui.browse.BrowseRecentCategories
import uk.co.sentinelweb.cuer.app.ui.common.views.description.DescriptionContract
import uk.co.sentinelweb.cuer.app.util.link.LinkExtractor
import uk.co.sentinelweb.cuer.app.util.link.TimecodeExtractor
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapper
import uk.co.sentinelweb.cuer.app.util.prefs.multiplatfom_settings.MultiPlatformPreferencesWrapperImpl
import uk.co.sentinelweb.cuer.app.util.recent.RecentLocalPlaylists

object SharedAppModule {
    private val queueModule = module {
        single<QueueMediatorContract.Producer> {
            QueueMediator(
                state = QueueMediatorState(),
                playlistOrchestrator = get(),
                playlistItemOrchestrator = get(),
                coroutines = get(),
                playlistMutator = get(),
                log = get(),
                prefsWrapper = get(),
                mediaUpdate = get(),
                playlistOrDefaultOrchestrator = get(),
                recentLocalPlaylists = get()
            )
        }
        single { get<QueueMediatorContract.Producer>() as QueueMediatorContract.Consumer }
    }

    private val orchestratorModule = module {
        single { PlaylistOrchestrator(get(), get()) }
        single { PlaylistItemOrchestrator(get(), get()) }
        single { MediaOrchestrator(get(), get()) }
        single { ChannelOrchestrator(get(), get()) }
        single { PlaylistStatsOrchestrator(get()) }
        factory { PlaylistUpdateOrchestrator(get(), get(), get(), get(), get(), get(), get()) }
        factory<PlaylistUpdateOrchestrator.UpdateCheck> { PlaylistUpdateOrchestrator.PlatformUpdateCheck() }
        factory { PlaylistMergeOrchestrator(get(), get()) }
        factory { PlaylistMediaLookupOrchestrator(get(), get()) }
        single { NewMediaPlayistInteractor(get(), get(), get(), get(named(NEWITEMS_PLAYLIST.toString()))) }
        single { RecentItemsPlayistInteractor(get(), get()) }
        single { StarredItemsPlayistInteractor(get(), get(), get(), get(named(STAR_PLAYLIST.toString()))) }
        single { UnfinishedItemsPlayistInteractor(get(), get(), get(), get(named(UNFINISHED_PLAYLIST.toString()))) }
        factory { AddLinkOrchestrator(get(), get(), get(), get(), get()) }
        single { LocalSearchPlayistInteractor(get(), get(), get()) }
        single {
            YoutubeSearchPlayistInteractor(
                get(),
                get(),
                get(),
                YoutubeSearchPlayistInteractor.State()
            )
        }
        factory { PlaylistMediaUpdateOrchestrator(get()) }
        factory { PlaylistOrDefaultOrchestrator(get(), get()) }
        factory {
            mapOf(
                NEWITEMS_PLAYLIST to get<NewMediaPlayistInteractor>(),
                RECENT_PLAYLIST to get<RecentItemsPlayistInteractor>(),
                LOCAL_SEARCH_PLAYLIST to get<LocalSearchPlayistInteractor>(),
                YOUTUBE_SEARCH_PLAYLIST to get<YoutubeSearchPlayistInteractor>(),
                STAR_PLAYLIST to get<StarredItemsPlayistInteractor>(),
                UNFINISHED_PLAYLIST to get<UnfinishedItemsPlayistInteractor>(),
            )
        }
    }

    private val objectModule = module {
        factory { ParserFactory() }
        single { PlaylistMemoryRepository(get(), get(), get(), get(), get(), get(), get()) }
        single { get<PlaylistMemoryRepository>().playlistItemMemoryRepository }
        single { get<PlaylistMemoryRepository>().mediaMemoryRepository }
        single<MultiPlatformPreferencesWrapper> { MultiPlatformPreferencesWrapperImpl() }
        factory { BrowseRecentCategories(get(), get()) }
        factory { RecentLocalPlaylists(get(), get(), get(), get()) }
        factory { PlatformFileOperation() }
        factory { LinkExtractor() }
        factory { TimecodeExtractor() }
        factory { BackupCheck(get(), get(), get()) }
    }

    val modules = listOf(objectModule)
        .plus(orchestratorModule)
        .plus(queueModule)
        .plus(DescriptionContract.viewModule)
}
