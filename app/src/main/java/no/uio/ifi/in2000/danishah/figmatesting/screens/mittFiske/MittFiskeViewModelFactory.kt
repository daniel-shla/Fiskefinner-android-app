package no.uio.ifi.in2000.danishah.figmatesting.screens.mittFiske

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import no.uio.ifi.in2000.danishah.figmatesting.data.repository.MittFiskeRepository

class MittFiskeViewModelFactory(
    private val repository: MittFiskeRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MittFiskeViewModel::class.java)) {
            return MittFiskeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
