package jobshop;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class ParsingTests {

    /** Test that instance discovery does not miss any instance. */
    @Test
    public void testInstanceDiscovery() {
        assert BestKnownResults.instancesMatching("ft").size() == 3 : "There should be 3 instances from Fisher & Thompson";
        assert BestKnownResults.instancesMatching("la").size() == 40 : "There should be 40 instances from Lawrence";
        assert BestKnownResults.instancesMatching("ft06").size() == 1 : "There should be exactly one instance with the ft06 name";
        assert BestKnownResults.instancesMatching("no_instance_with_this_name").size() == 0;
    }

    /** Test that the first Lawrence instance has the expected properties. */
    @Test
    public void testParsing() throws IOException {
        Instance instance = Instance.fromFile(Paths.get("instances/la01"));

        assert instance.numJobs == 10;
        assert instance.numTasks == 5;
        assert instance.numMachines == 5;
    }

    /** Test that we can successfully read all known instances. */
    @Test
    public void testAllParsable() throws IOException {
        for (String instanceName : BestKnownResults.instances) {
            Instance instance = Instance.fromFile(Paths.get("instances", instanceName));
            assert instance.name.equals(instanceName);
        }

    }


}
