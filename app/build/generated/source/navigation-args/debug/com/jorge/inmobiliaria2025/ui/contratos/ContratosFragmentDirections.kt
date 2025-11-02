package com.jorge.inmobiliaria2025.ui.contratos

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.jorge.inmobiliaria2025.R

public class ContratosFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionContratosFragmentToDetalleContratoFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_contratosFragment_to_detalleContratoFragment)
  }
}
