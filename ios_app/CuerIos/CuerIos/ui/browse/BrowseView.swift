//
//  BrowseView.swift
//  CuerIos
//
//  Created by Robert Munro on 14/11/2022.
//

import SwiftUI
import Combine
import shared

struct BrowseView: View {
    @StateObject
    private var view = BrowseViewProxy()
    
    @StateObject
    private var holder: ControllerHolder
    
    @StateObject private var viewModel: BrowseViewModel
    
    init(viewModel: BrowseViewModel) {
        self._viewModel = StateObject(wrappedValue: viewModel)
        let mainInput = PassthroughSubject<BrowseInput, Never>()
        _holder = StateObject(
            wrappedValue: ControllerHolder(input: mainInput.eraseToAnyPublisher()) { lifecycle in
                debugPrint("creating browse controller")
                return PresentationFactory().browseControllerCreate(lifecycle: lifecycle)
            }
        )
    }
    
    var body: some View {
        VStack {
            Text(view.model.title).onTapGesture {
                //viewModel.execPlatformRequest()
            }
        }
        .onFirstAppear { holder.controller.onViewCreated(views: [view], viewLifecycle: holder.lifecycle) }
//        .onAppear { LifecycleRegistry.resume(holder.lifecycle) }
//        .onDisappear { LifecycleRegistryExtKt.stop(holder.lifecycle) }
        .onAppear { holder.lifecycle.onStart();holder.lifecycle.onResume() }
        .onDisappear { holder.lifecycle.onPause();holder.lifecycle.onStop(); }
    }
}

private class ControllerHolder : ObservableObject {
    let utils = OrchestratorFactory().utils
    let lifecycle: LifecycleLifecycleRegistry
    let controller: BrowseController
    private var cancellable: AnyCancellable?

    init(input: AnyPublisher<BrowseInput, Never>, factory: (LifecycleLifecycle) -> BrowseController) {
        lifecycle = utils.lifecycleRegistry()
        lifecycle.onCreate()
        controller = factory(lifecycle)

        cancellable = input.sink { [weak self] in
            switch $0 {
//            case let .ItemChanged(id, data): self?.controller.onItemChanged(id: id, data: data)
//            case let .ItemDeleted(id): self?.controller.onItemDeleted(id: id)
            default: debugPrint($0)
            }
        }
    }

    deinit {
        cancellable?.cancel()
        //utils.destroyLifecycle(lifecycleReg: utils.lifecycleRegistry(), lifecycle: lifecycle)
//        LifecycleRegistryExtKt.destroy(lifecycle)
        lifecycle.onDestroy()
    }
}
//
class BCStrings:BrowseContractStrings {
    func errorNoCatWithID(id: Int64) -> String {
        "errorNoCatWithID"
    }
    
    var allCatsTitle: String = "allCatsTitle"
    
    var errorNoPlaylistConfigured: String = "errorNoPlaylistConfigured"
    
    var recent: String = "errorNoPlaylistConfigured"
    

}
//
private class BrowseViewProxy : UtilsUBaseView<BrowseContractViewModel, BrowseContractViewEvent>, BrowseContractView, ObservableObject {
    func processLabel(label: BrowseContractMviStoreLabel) {
        
    }
    
    @Published
    var model: BrowseContractViewModel
    
    override init() {
        model = BrowseContractViewModel(
            title: "browsetitle",
            categories: [],
            recent: nil,
            isRoot: true,
            order: BrowseContract.Order.categories)
        super.init()
    }

    override func render(model: BrowseContractViewModel) {
        self.model = model
    }
    
}
//
//
enum BrowseInput {
    case ItemChanged(id: String, data: String)
    case ItemDeleted(id: String)
}


//struct BrowseView_Previews: PreviewProvider {
//    static var previews: some View {
//        BrowseView()
//    }
//}
