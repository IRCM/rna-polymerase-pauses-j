/*
 * Copyright (c) 2017 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.rnapolymerasepauses.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ChromosomeSizesParserTest {
  private static final int CHROMOSOME_COUNT = 21;
  private static final int MAX_CHROMOSOME_LENGTH = 1000000;
  private ChromosomeSizesParser chromosomeSizesParser = new ChromosomeSizesParser();
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Map<String, Integer> sizes;

  private void createChromosomeSizes(Path path) throws IOException {
    Random random = new Random();
    sizes = IntStream.range(0, CHROMOSOME_COUNT).collect(() -> new HashMap<>(),
        (map, i) -> map.put("chr" + (i + 1), random.nextInt(MAX_CHROMOSOME_LENGTH)),
        (map1, map2) -> map1.putAll(map2));
    List<String> lines = IntStream.range(0, CHROMOSOME_COUNT)
        .mapToObj(i -> "chr" + (i + 1) + "\t" + sizes.get("chr" + (i + 1)))
        .collect(Collectors.toList());
    Files.write(path, lines);
  }

  @Test
  public void chromosomeSizes() throws Throwable {
    Path file = temporaryFolder.newFile("chromSizes.txt").toPath();
    createChromosomeSizes(file);

    Map<String, Long> sizes = chromosomeSizesParser.chromosomeSizes(file);

    for (int i = 1; i <= CHROMOSOME_COUNT; i++) {
      String chromosome = "chr" + i;
      assertTrue(sizes.containsKey(chromosome));
      assertEquals(this.sizes.get(chromosome).intValue(), sizes.get(chromosome).intValue());
    }
  }

  @Test(expected = IOException.class)
  public void chromosomeSizes_Invalid() throws Throwable {
    Path file = temporaryFolder.newFile("chromSizes_invalid.txt").toPath();
    List<String> lines = IntStream.range(0, CHROMOSOME_COUNT).mapToObj(i -> "chr" + (i + 1))
        .collect(Collectors.toList());
    Files.write(file, lines);

    chromosomeSizesParser.chromosomeSizes(file);
  }
}
