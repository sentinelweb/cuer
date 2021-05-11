package uk.co.sentinelweb.cuer.app.ui.search.image

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import org.koin.android.ext.android.inject
import org.koin.android.scope.currentScope
import uk.co.sentinelweb.cuer.app.databinding.FragmentComposeBinding
import uk.co.sentinelweb.cuer.app.ui.search.image.SearchImageViewModel.UiEvent.Type.ERROR
import uk.co.sentinelweb.cuer.app.util.firebase.FirebaseDefaultImageProvider
import uk.co.sentinelweb.cuer.app.util.wrapper.ToastWrapper
import uk.co.sentinelweb.cuer.core.wrapper.LogWrapper




class SearchImageDialogFragment(private val config: SearchImageContract.Config) : DialogFragment() {

    private val viewModel: SearchImageViewModel by currentScope.inject()
    private val log: LogWrapper by inject()
    private val toastWrapper: ToastWrapper by inject()
    private val imageProvider: FirebaseDefaultImageProvider by inject()

//    private lateinit var searchImageAdapter: SearchImageAdapter

    init {
        log.tag(this)
    }

    //    private var _binding: FragmentImageSearchBinding? = null
//    private val binding get() = _binding!!
    private var _binding: FragmentComposeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentComposeBinding.inflate(layoutInflater)
        viewModel.setConfig(config)
        binding.composeView.setContent {
            SearchImageView(viewModel = viewModel)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding.searchimageList.layoutManager = LinearLayoutManager(context)
//        searchImageAdapter = SearchImageAdapter(viewModel::onImageSelected, imageProvider)
//        binding.searchimageList.adapter = searchImageAdapter
//        binding.searchimageTermEdit.doAfterTextChanged { text -> viewModel.onSearchTextChange(text.toString()) }
//        binding.searchimageTermEdit.setOnEditorActionListener({ v, actionId, event ->
//            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                viewModel.onSearch()
//                true
//            } else false
//        })

        observeUi()
        //observeModel()
    }

    private fun observeUi() {
        viewModel.getUiObservable().observe(
            this.viewLifecycleOwner,
            object : Observer<SearchImageViewModel.UiEvent> {
                override fun onChanged(model: SearchImageViewModel.UiEvent) {
                    when (model.type) {
                        ERROR -> toastWrapper.show(model.data as String)
                        //LOADING -> binding.searchimageProgress.isVisible = (model.data as Boolean)
                        else -> Unit
                    }
                }
            })
    }

//    private fun observeModel() {
//        viewModel.getModelObservable().observe(
//            this.viewLifecycleOwner,
//            object : Observer<SearchImageContract.Model> {
//                override fun onChanged(model: SearchImageContract.Model) {
//                    searchImageAdapter.setData(model.images)
//                    if (binding.searchimageTermEdit.text.toString() != model.term) {
//                        binding.searchimageTermEdit.setText(model.term)
//                        binding.searchimageTermEdit.setSelection(model.term?.length ?: 0)
//                    }
//                }
//            })
//    }

    companion object {

        fun newInstance(config: SearchImageContract.Config): SearchImageDialogFragment {
            return SearchImageDialogFragment(config)
        }

    }
}