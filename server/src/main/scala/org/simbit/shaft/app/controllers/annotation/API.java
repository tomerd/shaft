package org.simbit.shaft.app.controllers.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface API 
{
	long action() default 0;
	boolean loginRequired() default true;
	boolean sslRequired() default false;
}