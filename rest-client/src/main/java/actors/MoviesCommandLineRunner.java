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

package actors;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.Assert;

@Component
public class MoviesCommandLineRunner implements CommandLineRunner {

	private final TransactionTemplate transactionTemplate;

	private final ActorRepository actorRepository;

	private final MovieRepository movieRepository;

	@Autowired
	public MoviesCommandLineRunner(TransactionTemplate transactionTemplate,
			ActorRepository actorRepository, MovieRepository movieRepository) {
		this.transactionTemplate = transactionTemplate;
		this.actorRepository = actorRepository;
		this.movieRepository = movieRepository;
	}

	@Override
	public void run(String... strings) throws Exception {

		this.transactionTemplate
				.execute(tx -> Stream
						.of("Cars (Owen Wilson,Paul Newman,Bonnie Hunt)",
								"Batman (Michael Keaton,Jack Nicholson)",
								"Lost in Translation (Bill Murray)")
						.map(String::trim).map(i -> {
							Matcher matcher = Pattern.compile("(.*?)\\s*?\\((.*?)\\)")
									.matcher(i);
							Assert.state(matcher.matches());
							Movie movie = movieRepository
									.save(new Movie(matcher.group(1)));
							Arrays.stream(matcher.group(2).split(",")).map(String::trim)
									.forEach(a -> {
										Actor actor = actorRepository
												.save(new Actor(a.trim(), movie));
										movie.actors
												.add(actorRepository.findOne(actor.id));
										movieRepository.save(movie);
									});
							return movieRepository.findOne(movie.id);
						}).collect(Collectors.toList()));
	}

}
