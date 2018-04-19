package com.blacklabelops.crow.console.definition;

import org.immutables.value.Value;

import com.blacklabelops.crow.console.ImmutableStyle;

@Value.Immutable
@ImmutableStyle
public interface AbstractJobId {

	@Value.Parameter(order = 1)
	String getId();
}
