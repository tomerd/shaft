package org.simbit.shaft.app.controllers.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface API 
{
	String alias() default "";
}