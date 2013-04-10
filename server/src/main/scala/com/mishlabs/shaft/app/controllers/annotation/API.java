package com.mishlabs.shaft.app.controllers.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface API 
{
	String alias() default "";
}