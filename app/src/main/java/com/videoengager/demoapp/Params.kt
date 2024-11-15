//
// DemoPureCloud
//
// Copyright © 2021 VideoEngager. All rights reserved.
//
package com.videoengager.demoapp

import com.videoengager.sdk.model.Settings

data class Params(
    val genesys_cloud_params_init:Settings,
    val generic_params_init:Settings,
    val genesys_engage_params_init:Settings,
    val genesys_cloud_messaging_params_init:Settings,
)
