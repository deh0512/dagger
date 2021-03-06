/*
 * Copyright (C) 2014 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.producers;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * An interface that represents the production of a type {@code T}. You can also inject
 * {@code Producer<T>} instead of {@code T}, which will delay the execution of any code that
 * produces the {@code T} until {@link #get} is called.
 *
 * <p>For example, you might inject {@code Producer} to lazily choose between several different
 * implementations of some type: <pre>   {@code
 *
 *   @Produces ListenableFuture<Heater> getHeater(
 *       HeaterFlag flag,
 *       @Electric Producer<Heater> electricHeater,
 *       @Gas Producer<Heater> gasHeater) {
 *     return flag.useElectricHeater() ? electricHeater.get() : gasHeater.get();
 *   }}</pre>
 *
 * <p>Here is a complete example that demonstrates how calling {@code get()} will cause each
 * method to be executed: <pre>   {@code
 *
 *   @ProducerModule
 *   final class MyModule {
 *     @Produces ListenableFuture<A> a() {
 *       System.out.println("a");
 *       return Futures.immediateFuture(new A());
 *     }
 *
 *     @Produces ListenableFuture<B> b(A a) {
 *       System.out.println("b");
 *       return Futures.immediateFuture(new B(a));
 *     }
 *
 *     @Produces ListenableFuture<C> c(B b) {
 *       System.out.println("c");
 *       return Futures.immediateFuture(new C(b));
 *     }
 *
 *     @Produces @Delayed ListenableFuture<C> delayedC(A a, Producer<C> c) {
 *       System.out.println("delayed c");
 *       return c.get();
 *     }
 *   }
 *
 *   @ProductionComponent(modules = MyModule.class)
 *   interface MyComponent {
 *     @Delayed ListenableFuture<C> delayedC();
 *   }}</pre>
 * Suppose we instantiate the generated implementation of this component and call
 * {@code delayedC()}: <pre>   {@code
 *
 *   MyComponent component = Dagger_MyComponent
 *       .builder()
 *       .executor(MoreExecutors.directExecutor())
 *       .build();
 *   System.out.println("Constructed component");
 *   ListenableFuture<C> cFuture = component.delayedC();
 *   System.out.println("Retrieved future");
 *   C c = cFuture.get();
 *   System.out.println("Retrieved c");}</pre>
 * Here, we're using {@code MoreExecutors.directExecutor} in order to illustrate how each call
 * directly causes code to execute. The above code will print: <pre>   {@code
 *   Constructed component
 *   a
 *   delayed c
 *   b
 *   c
 *   Retrieved future
 *   Retrieved c}</pre>
 *
 * @author Jesse Beder
 */
public interface Producer<T> {
  /**
   * Returns a future representing a running task that produces a value. Calling this method will
   * trigger the submission of this task to the executor, if it has not already been triggered. In
   * order to trigger this task's submission, the transitive dependencies required to produce the
   * {@code T} will be submitted to the executor, as their dependencies become available.
   *
   * <p>If the key is bound to a {@link Produces} method, then calling this method multiple times
   * will return the same future.
   */
  ListenableFuture<T> get();
}
