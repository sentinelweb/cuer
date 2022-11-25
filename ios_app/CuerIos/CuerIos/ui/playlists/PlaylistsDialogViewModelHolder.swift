//
//  PlaylistsDialogViewModelHolder.swift
//  CuerIos
//
//  Created by Robert Munro on 25/11/2022.
//

import Foundation
import shared
import KMPNativeCoroutinesCombine
import Combine

protocol PlaylistsDialogDependency {
    func createPlaylistsDialogHolder(config: PlaylistsMviDialogContractConfig) -> PlaylistsDialogViewModelHolder
}

class PlaylistsDialogProvider: PlaylistsDialogViewModelHolder.Dependencies {
    let viewModel: PlaylistsDialogViewModel
    let config: PlaylistsMviDialogContractConfig
    let mainCoordinator: MainCoordinator
    
    init(
        mainCoordinator: MainCoordinator,
        viewModel: PlaylistsDialogViewModel,
        config: PlaylistsMviDialogContractConfig
    ) {
        self.mainCoordinator = mainCoordinator
        self.config = config
        self.viewModel = viewModel
    }
}

protocol PlaylistsDialogDependencies {
    var viewModel: PlaylistsDialogViewModel {get}
    var config: PlaylistsMviDialogContractConfig  {get}
}

class PlaylistsDialogViewModelHolder: ObservableObject {
    typealias Dependencies = MainCoordinatorDependency & PlaylistsDialogDependencies
    
    private let dependencies: Dependencies
    private let viewModel: PlaylistsDialogViewModel
    
    @Published
    var model: PlaylistsMviDialogContractModel!
    
    private var modelCancellable: AnyCancellable? = nil
    private var labelCancellable: AnyCancellable? = nil
    
    // viewModel: PlaylistsDialogViewModel, config: PlaylistsMviDialogContractConfig
    init(dependencies: Dependencies) {
        self.dependencies = dependencies
        self.viewModel = dependencies.viewModel
        self.viewModel.setConfig(config: dependencies.config)
        // todo publish config to view
        
        let modelPublisher = createPublisher(for: self.viewModel.modelNative)
        modelCancellable = modelPublisher
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: {error in debugPrint(error)},
                receiveValue: {model in
                    debugPrint("playlist dialog model received: \(model.playistsModel?.items.count)")
                    self.model = model
                })
        
        let labelPublisher = createPublisher(for: self.viewModel.labelNative)
        labelCancellable = labelPublisher
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: {error in debugPrint(error)},
                receiveValue: {label in
                    debugPrint("playlist dialog label received: \(label)")
                    switch(label) {
                    case .dismiss: dependencies.mainCoordinator.hidePlaylistSelector()
                    default: debugPrint("invalid label \(label)")
                    }
                })
        self.viewModel.onResume()
    }
    
    func onItemSelected(item:PlaylistsItemMviContract.ModelItem) {
        self.viewModel.onItemClicked(item: item)
    }
    
    func onUpClick() {
        self.viewModel.onDismiss()
    }
    
}
