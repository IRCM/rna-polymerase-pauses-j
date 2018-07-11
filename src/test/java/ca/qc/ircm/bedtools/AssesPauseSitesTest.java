package ca.qc.ircm.bedtools;

import ca.qc.ircm.bedtools.test.config.NonTransactionalTestAnnotations;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class AssesPauseSitesTest {
  private AssesPauseSites assesPauseSites;

  @Before
  public void beforeTest() {
    assesPauseSites = new AssesPauseSites();
  }
}
