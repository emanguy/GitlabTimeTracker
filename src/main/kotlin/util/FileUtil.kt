package edu.erittenhouse.gitlabtimetracker.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

val JsonMapper = ObjectMapper().registerKotlinModule()
