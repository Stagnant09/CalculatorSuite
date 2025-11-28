package org.calculator.ui.screens

import com.example.calculator.foundation.CustomViewModel

class XYViewmodel : CustomViewModel<XYContract.State, XYContract.Event, XYContract.Effect>(
    initialState = XYContract.State()
) {
    override suspend fun handleEvent(event: XYContract.Event) {

    }
}