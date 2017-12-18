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

package ca.qc.ircm.bedtools;

import static ca.qc.ircm.bedtools.MoveAnnotationsCommand.MOVE_ANNOTATIONS_COMMAND;
import static ca.qc.ircm.bedtools.SetAnnotationsSizeCommand.SET_ANNOTATIONS_SIZE_COMMAND;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import ca.qc.ircm.bedtools.BedTransform;
import ca.qc.ircm.bedtools.MainService;
import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
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
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1" });
    verifyZeroInteractions(bedTransform);
  }

  @Test
  public void run_Help() {
    mainService.run("-h");
    verifyZeroInteractions(bedTransform);
  }

  @Test
  public void run_SetAnnotationsSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "1" });
    verify(bedTransform).setAnnotationsSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationsSize_LongName() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-size", "1" });
    verify(bedTransform).setAnnotationsSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationsSize_UpperCase() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND.toUpperCase(), "-s", "1" });
    verify(bedTransform).setAnnotationsSize(System.in, System.out, 1);
  }

  @Test
  public void run_SetAnnotationsSize_InvalidSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "a" });
    verify(bedTransform, never()).setAnnotationsSize(any(), any(), anyInt());
  }

  @Test
  public void run_SetAnnotationsSize_NegativeSize() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-s", "-2" });
    verify(bedTransform, never()).setAnnotationsSize(any(), any(), anyInt());
  }

  @Test
  public void run_SetAnnotationsSize_Help() throws Throwable {
    mainService.run(new String[] { SET_ANNOTATIONS_SIZE_COMMAND, "-h", "-s", "1" });
    verify(bedTransform, never()).setAnnotationsSize(any(), any(), anyInt());
  }

  @Test
  public void run_MoveAnnotations() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "20" });
    verify(bedTransform).moveAnnotations(System.in, System.out, 20);
  }

  @Test
  public void run_MoveAnnotations_LongName() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-distance", "20" });
    verify(bedTransform).moveAnnotations(System.in, System.out, 20);
  }

  @Test
  public void run_MoveAnnotations_UpperCase() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND.toUpperCase(), "-d", "20" });
    verify(bedTransform).moveAnnotations(System.in, System.out, 20);
  }

  @Test
  public void run_MoveAnnotations_InvalidSize() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "a" });
    verify(bedTransform, never()).moveAnnotations(any(), any(), anyInt());
  }

  @Test
  public void run_MoveAnnotations_NegativeSize() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-d", "-30" });
    verify(bedTransform).moveAnnotations(System.in, System.out, -30);
  }

  @Test
  public void run_MoveAnnotations_Help() throws Throwable {
    mainService.run(new String[] { MOVE_ANNOTATIONS_COMMAND, "-h", "-d", "1" });
    verify(bedTransform, never()).moveAnnotations(any(), any(), anyInt());
  }

  @Test
  public void run_Other() throws Throwable {
    mainService.run(new String[] { "other" });
    verifyZeroInteractions(bedTransform);
  }
}
