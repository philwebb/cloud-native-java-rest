/*
 * Copyright 2017-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package demo;

import java.util.function.Function;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public abstract class TestUtils {

	public static <T> LambdaMatcher<T> lambaMatcher(String d, Function<T, Boolean> f) {
		return new LambdaMatcher<>(f, d);
	}

	// taken from
	// https://gist.github.com/GuiSim/e1d1cde0ab66302ae45c
	private static class LambdaMatcher<T> extends BaseMatcher<T> {

		private final Function<T, Boolean> matcher;

		private final String description;

		public LambdaMatcher(Function<T, Boolean> matcher, String description) {
			this.matcher = matcher;
			this.description = description;
		}

		@Override
		public boolean matches(Object argument) {
			return matcher.apply((T) argument);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText(this.description);
		}

	}

}
