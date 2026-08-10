package net.trueog.unionsog.ui.frames;

import com.cryptomorin.xseries.XMaterial;
import net.trueog.unionsog.Proposal;
import net.trueog.unionsog.UnionPlayer;
import net.trueog.unionsog.Union;
import net.trueog.unionsog.UnionsOG;
import net.trueog.unionsog.ui.InventoryController;
import net.trueog.unionsog.ui.InventoryDrawer;
import net.trueog.unionsog.ui.SCComponent;
import net.trueog.unionsog.ui.SCComponentImpl;
import net.trueog.unionsog.ui.SCFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;

import static net.trueog.unionsog.UnionsOG.lang;

/**
 * Lets a member cast their vote on their union's open proposal.
 */
public class ProposalFrame extends SCFrame {

    private final UnionsOG plugin = UnionsOG.getInstance();
    private final Union union;

    public ProposalFrame(@Nullable SCFrame parent, @NotNull Player viewer, @NotNull Union union) {

        super(parent, viewer);
        this.union = union;

    }

    @Override
    public @NotNull String getTitle() {

        return lang("gui.proposal.title", getViewer());

    }

    @Override
    public int getSize() {

        return 3 * 9;

    }

    @Override
    public void createComponents() {

        if (getParent() != null) {

            add(Components.getBackComponent(getParent(), 0, getViewer()));

        }

        Proposal proposal = plugin.getProposalManager().getProposal(union);
        if (proposal == null) {

            add(new SCComponentImpl.Builder(XMaterial.BARRIER.parseItem())
                    .withDisplayName(lang("gui.proposal.none.title", getViewer())).withSlot(13).build());
            return;

        }

        add(new SCComponentImpl.Builder(XMaterial.PAPER.parseItem())
                .withDisplayName(lang("gui.proposal.subject.title", getViewer()))
                .withLore(Collections.singletonList(plugin.getProposalManager().describe(proposal, union))).withSlot(13)
                .build());

        SCComponent yes = new SCComponentImpl.Builder(XMaterial.LIME_WOOL.parseItem())
                .withDisplayName(lang("gui.proposal.yes.title", getViewer()))
                .withLore(Arrays.asList(lang("gui.proposal.yes.lore", getViewer()))).withSlot(11).build();
        yes.setListener(ClickType.LEFT, () -> vote("yes"));
        add(yes);

        SCComponent no = new SCComponentImpl.Builder(XMaterial.RED_WOOL.parseItem())
                .withDisplayName(lang("gui.proposal.no.title", getViewer()))
                .withLore(Arrays.asList(lang("gui.proposal.no.lore", getViewer()))).withSlot(15).build();
        no.setListener(ClickType.LEFT, () -> vote("no"));
        add(no);

    }

    private void vote(@NotNull String choice) {

        // Goes through the command so the vote takes the same permission and
        // membership checks as /union vote.
        InventoryController.runSubcommand(getViewer(), "vote " + choice, false);
        getViewer().closeInventory();

    }

    /**
     * Opens the frame for a member who has not voted on their union's open proposal
     * yet.
     *
     * @param player the member
     * @return whether the frame was opened
     */
    public static boolean openIfPending(@NotNull Player player) {

        UnionsOG plugin = UnionsOG.getInstance();
        UnionPlayer cp = plugin.getUnionManager().getUnionPlayer(player);
        Union union = cp != null ? cp.getUnion() : null;
        Proposal proposal = plugin.getProposalManager().getProposal(union);

        if (union == null || proposal == null || proposal.hasVoted(player.getUniqueId())) {

            return false;

        }

        InventoryDrawer.open(new ProposalFrame(null, player, union));
        return true;

    }

}
