package me.zeroeightsix.kami.command.commands;

import io.netty.buffer.Unpooled;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author 0x2E | PretendingToCode
 */
public class DupeBookCommand extends Command {

    public DupeBookCommand() {
        super("dupebook", new ChunkBuilder().append("name").build());
        setDescription("Generates books used for chunk savestate dupe.");
    }

    @Override
    public void call(String[] args) {
        ItemStack is = Wrapper.getPlayer().inventory.getCurrentItem();

        if (is.getItem() instanceof ItemWritableBook) {
            IntStream characterGenerator = new Random().ints(0x80, 0x10ffff - 0x800).map(i -> i < 0xd800 ? i : i + 0x800);
            NBTTagList pages = new NBTTagList();
            String joinedPages = characterGenerator.limit(50 * 210).mapToObj(i -> String.valueOf((char) i)).collect(Collectors.joining());

            for (int page = 0; page < 50; page++) {
                pages.appendTag(new NBTTagString(joinedPages.substring(page * 210, (page + 1) * 210)));
            }

            if(is.hasTagCompound()){
                is.getTagCompound().setTag("pages", pages);
                is.getTagCompound().setTag("title", new NBTTagString(""));
                is.getTagCompound().setTag("author", new NBTTagString(Wrapper.getPlayer().getName()));
            } else {
                is.setTagInfo("pages", pages);
                is.setTagInfo("title", new NBTTagString(""));
                is.setTagInfo("author", new NBTTagString(Wrapper.getPlayer().getName()));
            }

            PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
            buf.writeItemStack(is);

            Wrapper.getPlayer().connection.sendPacket(new CPacketCustomPayload("MC|BEdit", buf));
            Command.sendChatMessage("Dupe book generated.");
        } else {
            Command.sendChatMessage("You must be holding a writable book.");
        }
    }
}