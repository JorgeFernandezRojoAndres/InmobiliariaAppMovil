package com.jorge.inmobiliaria2025.ui.Inmueble

import androidx.`annotation`.CheckResult
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.jorge.inmobiliaria2025.R

public class InmueblesFragmentDirections private constructor() {
  public companion object {
    @CheckResult
    public fun actionInmueblesFragmentToDetalleInmuebleFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_inmueblesFragment_to_detalleInmuebleFragment)

    @CheckResult
    public fun actionInmueblesFragmentToNuevoInmuebleFragment(): NavDirections = ActionOnlyNavDirections(R.id.action_inmueblesFragment_to_nuevoInmuebleFragment)
  }
}
