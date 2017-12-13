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

package ca.qc.ircm.htstools;

import static ca.qc.ircm.htstools.SetAnnotationSizeCommand.SET_ANNOTATIONS_SIZE_COMMAND;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import ca.qc.ircm.htstools.test.config.NonTransactionalTestAnnotations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class MainServiceTest {
  private MainService mainService;
  @Mock
  private BedTransform bedTransform;

  @Before
  public void beforeTest() {
    mainService = new MainService(bedTransform, true);
  }

  @Test
  public void run_RunnerDisabled() {
    mainService = new MainService(bedTransform, false);
    mainService.run(new String[] { "setAnnotationSize", "-s", "1" });
    verifyZeroInteractions(bedTransform);
  }

  @Test
  public void run_Help() {
    mainService.run("-h");
    verifyZeroInteractions(bedTransform);
  }

  @Test
  public void run_SetAnnotationSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1" });
    verify(bedTransform).setAnnotationSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationSize_LongName() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-size", "1" });
    verify(bedTransform).setAnnotationSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationSize_UpperCase() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND.toUpperCase(), "-size", "1" });
    verify(bedTransform).setAnnotationSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationSize_InvalidSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-size", "a" });
    verify(bedTransform, never()).setAnnotationSize(any(), any(), anyInt());
  }

  @Test
  public void run_SetAnnotationSize_NegativeSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-size", "-2" });
    verify(bedTransform, never()).setAnnotationSize(any(), any(), anyInt());
  }

  @Test
  public void run_SetAnnotationSize_Help() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-h", "-size", "1" });
    verify(bedTransform, never()).setAnnotationSize(any(), any(), anyInt());
  }

  @Test
  public void run_Other() throws Throwable {
    mainService.run(new String[] { "other" });
    verify(bedTransform, never()).setAnnotationSize(any(), any(), anyInt());
  }
}
