//
//  BrowseMviController.swift
//  CuerIos
//
//  Created by Robert Munro on 18/11/2022.
//

import Foundation
import KMPNativeCoroutinesAsync
import shared

protocol BrowseControllerDependency {
    func createBrowseHolder() -> BrowseControllerHolder
}

class BrowseControllerProvider: BrowseControllerHolder.Dependencies {
    let mainCoordinator: MainCoordinator
    let sharedFactories: SharedFactories
    
    init(
        mainCoordinator: MainCoordinator,
        sharedFactories: SharedFactories
    ) {
        self.mainCoordinator = mainCoordinator
        self.sharedFactories = sharedFactories
    }
}

class BrowseControllerHolder : ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & SharedFactoriesDependency
    
    private let dependencies: Dependencies
    let lifecycle: LifecycleLifecycleRegistry
    let controller: BrowseController

    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        lifecycle = dependencies.sharedFactories.orchestratorFactory.utils.lifecycleRegistry()
        controller = dependencies.sharedFactories.presentationFactory.browseControllerCreate(lifecycle: lifecycle)
        lifecycle.onCreate()
    }

    deinit {
        lifecycle.onDestroy()
    }
    
    func createMviView() -> BrowseViewProxy { BrowseViewProxy(dependencies:self.dependencies)}
}

class BCStrings:BrowseContractStrings {
    func errorNoCatWithID(id: Int64) -> String {
        "errorNoCatWithID"
    }
    
    var allCatsTitle: String = "allCatsTitle"
    
    var errorNoPlaylistConfigured: String = "errorNoPlaylistConfigured"
    
    var recent: String = "errorNoPlaylistConfigured"
}

class BrowseViewProxy : UtilsUBaseView<BrowseContractViewModel, BrowseContractViewEvent>, BrowseContractView, ObservableObject {
    @Published
    var model: BrowseContractViewModel
    
    @Published
    var loading: Bool = false
    
    private let dependencies: BrowseControllerHolder.Dependencies

    //let taskHandler: ((Task<(), Never>) ->Void)?
    
    init(dependencies: BrowseControllerHolder.Dependencies)  {
        model = BrowseContractViewModel(
            title: "browsetitle",
            categories: [],
            recent: nil,
            isRoot: true,
            order: BrowseContract.Order.categories)
        self.dependencies = dependencies
        super.init()
    }

    override func render(model: BrowseContractViewModel) {
        self.model = model
    }
    
    func processLabel(label: BrowseContractMviStoreLabel) {
        switch(label) {
        case is BrowseContractMviStoreLabel.AddPlaylist:
            let addLabel = label as! BrowseContractMviStoreLabel.AddPlaylist
            
            addPlaylist(cat: addLabel.cat, parentId: addLabel.parentId)
        case let openPlaylist as BrowseContractMviStoreLabel.OpenLocalPlaylist:
            self.dependencies.mainCoordinator.navigate(route: .playlist(plId: Int(openPlaylist.id)))
            
        default: debugPrint(label)
        }
    }
    
    func addPlaylist(cat: DomainCategoryDomain, parentId: KotlinLong?) {
        loading = true
        /*let task = */Task {
            do {
                let result = try await asyncFunction(
                    for: dependencies.sharedFactories.orchestratorFactory.addBrowsePlaylistUsecaseExecute(category: cat, parentId: parentId) //parentId
                )
                if result != nil {
                    if let playlist = result {
                        DispatchQueue.main.async {
                            self.loading = false
                            self.dependencies.mainCoordinator.navigate(route: .playlists)
                        }
                    }
                } else { debugPrint("addBrowsePlaylistUsecase: no data") }
            } catch {
                print("addBrowsePlaylistUsecase: Failed with error: \(error)")
                loading = false
            }
        }
        //self.taskHandler?(task)
        
    }
}
