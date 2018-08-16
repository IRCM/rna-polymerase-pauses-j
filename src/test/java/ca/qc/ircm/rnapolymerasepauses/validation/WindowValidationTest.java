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

package ca.qc.ircm.rnapolymerasepauses.validation;

import static org.junit.Assert.fail;

import ca.qc.ircm.rnapolymerasepauses.test.config.NonTransactionalTestAnnotations;
import com.beust.jcommander.ParameterException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class WindowValidationTest {
  private WindowValidation windowValidation = new WindowValidation();

  @Test
  public void validate() throws Throwable {
    try {
      windowValidation.validate("my-name", "20");
      // Success
    } catch (ParameterException e) {
      fail("ParameterException not expected");
    }
  }

  @Test
  public void validate_One() throws Throwable {
    try {
      windowValidation.validate("my-name", "1");
      // Success
    } catch (ParameterException e) {
      fail("ParameterException not expected");
    }
  }

  @Test
  public void validate_Zero() {
    try {
      windowValidation.validate("my-name", "0");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }

  @Test
  public void validate_Negative() {
    try {
      windowValidation.validate("my-name", "-1");
      fail("Expected ParameterException");
    } catch (ParameterException e) {
      // Success
    }
  }

  @Test
  public void validate_Invalid() {
    try {
      windowValidation.validate("my-name", "a");
      fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
      // Success
    }
  }

  @Test
  public void validate_Double() {
    try {
      windowValidation.validate("my-name", "1.2");
      fail("Expected NumberFormatException");
    } catch (NumberFormatException e) {
      // Success
    }
  }
}
