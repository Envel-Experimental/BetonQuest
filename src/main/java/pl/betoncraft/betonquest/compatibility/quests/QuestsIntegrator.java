package pl.betoncraft.betonquest.compatibility.quests;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import pl.betoncraft.betonquest.BetonQuest;
import pl.betoncraft.betonquest.compatibility.Integrator;


@SuppressWarnings("PMD.CommentRequired")
public class QuestsIntegrator implements Integrator {

    private static Quests questsInstance;
    private final BetonQuest plugin;

    public QuestsIntegrator() {
        plugin = BetonQuest.getInstance();
    }

    public static Quests getQuestsInstance() {
        return questsInstance;
    }

    @Override
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    public void hook() {
        questsInstance = (Quests) Bukkit.getPluginManager().getPlugin("Quests");
        plugin.registerConditions("quest", QuestsCondition.class);
        plugin.registerEvents("quest", QuestsEvent.class);
        questsInstance.getCustomRewards().add(new EventReward());
        questsInstance.getCustomRequirements().add(new ConditionRequirement());
    }

    @Override
    public void reload() {
        // Empty
    }

    @Override
    public void close() {
        // Empty
    }

}
