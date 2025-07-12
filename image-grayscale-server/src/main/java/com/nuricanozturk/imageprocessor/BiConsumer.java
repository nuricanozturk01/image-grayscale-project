package com.nuricanozturk.imageprocessor;

@FunctionalInterface
public interface BiConsumer<T, U> {
  void accept(T t, U u) throws Exception;
}
