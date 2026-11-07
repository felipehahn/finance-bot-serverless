package com.financebot;

import com.financebot.core.event.TelegramCommandEvent;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = TelegramCommandEvent.class)
public class NativeReflectionConfiguration {
}
