package de.SparkArmy.jda.events.customEvents.commandEvents;

import de.SparkArmy.controller.ConfigController;
import de.SparkArmy.db.Postgres;
import de.SparkArmy.jda.events.annotations.JDAButton;
import de.SparkArmy.jda.events.annotations.JDAModal;
import de.SparkArmy.jda.events.annotations.JDAStringMenu;
import de.SparkArmy.jda.events.customEvents.EventDispatcher;
import de.SparkArmy.utils.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.interactions.modals.ModalMapping;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ResourceBundle;

public class NoteSlashCommandEvents {

    private final JDA jda;

    private final Postgres db;

    final @NotNull Button nextButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_next;%s,%s;%d", actionType.getName(), ids[0], ids[1], count),
                bundle.getString("events.showEmbedButtons.next.label"));
    }

    final @NotNull Button beforeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, int count, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_before;%s,%s;%d", actionType.getName(), ids[0], ids[1], count),
                bundle.getString("events.showEmbedButtons.before.label"));
    }

    final @NotNull Button closeButton(@NotNull ResourceBundle bundle, String @NotNull [] ids, @NotNull NoteEmbedActionType actionType) {
        return Button.of(ButtonStyle.SECONDARY,
                String.format("noteCommand_%sNoteEmbed_close;%s", actionType.getName(), ids[0]),
                bundle.getString("events.showEmbedButtons.close.label"));
    }

    public NoteSlashCommandEvents(@NotNull EventDispatcher dispatcher) {
        ConfigController controller = dispatcher.getController();
        this.jda = dispatcher.getApi().getJda();
        this.db = controller.getMain().getPostgres();
    }

    @JDAButton(startWith = "noteCommand_initialShowEmbed_show")
    public void showButtonClickEvent(@NotNull ButtonInteractionEvent event) {
        sendInitialEmbed(event, NoteEmbedActionType.SHOW);
    }

    @JDAButton(startWith = "noteCommand_initialShowEmbed_edit")
    public void editButtonClickEvent(ButtonInteractionEvent event) {
        sendInitialEmbed(event, NoteEmbedActionType.EDIT);
    }

    @JDAButton(startWith = "noteCommand_initialShowEmbed_remove")
    public void removeButtonClickEvent(ButtonInteractionEvent event) {
        sendInitialEmbed(event, NoteEmbedActionType.REMOVE);
    }

    @JDAButton(startWith = "noteCommand_showNoteEmbed_close")
    public void closeShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        closeEmbed(event);
    }

    @JDAButton(startWith = "noteCommand_editNoteEmbed_close")
    public void closeEditEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        closeEmbed(event);
    }

    @JDAButton(startWith = "noteCommand_removeNoteEmbed_close")
    public void closeRemoveEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        closeEmbed(event);
    }

    @JDAButton(startWith = "noteCommand_showNoteEmbed_next")
    public void nextShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.SHOW);
    }

    @JDAButton(startWith = "noteCommand_editNoteEmbed_next")
    public void nextEditEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.EDIT);
    }

    @JDAButton(startWith = "noteCommand_removeNoteEmbed_next")
    public void nextRemoveEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.NEXT, NoteEmbedActionType.REMOVE);
    }

    @JDAButton(startWith = "noteCommand_showNoteEmbed_before")
    public void beforeShowEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.SHOW);
    }

    @JDAButton(startWith = "noteCommand_editNoteEmbed_before")
    public void beforeEditEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.EDIT);
    }

    @JDAButton(startWith = "noteCommand_removeNoteEmbed_before")
    public void beforeRemoveEmbedClickEvent(@NotNull ButtonInteractionEvent event) {
        sendShowEmbed(event, ClickType.BEFORE, NoteEmbedActionType.REMOVE);
    }

    private void closeEmbed(@NotNull ButtonInteractionEvent event) {
        String[] splitId = event.getComponentId().split(";");
        String commandUserId = splitId[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
        event.deferEdit().setEmbeds().setComponents().setContent(bundle.getString("events.closeShowEmbedClickEvent.closeMessage")).queue();
    }

    private void sendInitialEmbed(@NotNull ButtonInteractionEvent event, NoteEmbedActionType actionType) {
        if (event.getGuild() == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String commandUserId = ids[0];
        String targetUserId = ids[1];
        if (!event.getUser().getId().equals(commandUserId)) return;

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), event.getGuild().getIdLong());

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        showNoteEmbed.setDescription(bundle.getString("events.showButtonClickEvent.showNoteEmbed.description"));
        showNoteEmbed.clearFields();

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(
                String.format("noteCommand_showNoteEmbed_%sMenu;%s,%s", actionType.getName(), commandUserId, targetUserId));

        RestAction.allOf(setEmbedFieldsAndGetModerators(showNoteEmbed, menuBuilder, 0, notes)).mapToResult()
                .queue(x -> {
                    ActionRow actionRow;
                    if (notes.length() > 25) {
                        actionRow = ActionRow.of(
                                nextButton(bundle, ids, 25, actionType),
                                closeButton(bundle, ids, actionType));
                    } else {
                        actionRow = ActionRow.of(closeButton(bundle, ids, actionType));
                    }
                    switch (actionType) {
                        case SHOW ->
                                event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
                        case EDIT, REMOVE ->
                                event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow, ActionRow.of(menuBuilder.build())).queue();
                    }
                });
    }

    private void sendShowEmbed(@NotNull ButtonInteractionEvent event, ClickType clicktype, NoteEmbedActionType actionType) {
        if (event.getGuild() == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        String commandUserId = ids[0];
        String targetUserId = ids[1];
        int count = Integer.parseInt(splitId[2]);
        if (!ids[0].equals(event.getUser().getId())) return;

        EmbedBuilder showNoteEmbed = new EmbedBuilder(event.getMessage().getEmbeds().get(0));
        showNoteEmbed.clearFields();

        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create(
                String.format("noteCommand_showNoteEmbed_%sMenu;%s,%s", actionType.getName(), commandUserId, targetUserId));

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(targetUserId), event.getGuild().getIdLong());

        RestAction.allOf(setEmbedFieldsAndGetModerators(showNoteEmbed, menuBuilder, count, notes)).mapToResult()
                .queue(x -> {
                    ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
                    ActionRow actionRow;
                    int notesSize = notes.keySet().size();
                    if (clicktype.equals(ClickType.BEFORE)) {
                        if (count - 25 > 0) {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25, actionType),
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25), actionType),
                                    closeButton(bundle, ids, actionType));
                        } else {
                            actionRow = ActionRow.of(
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25), actionType),
                                    closeButton(bundle, ids, actionType));
                        }
                    } else {
                        if (notesSize - count > 1) {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25, actionType),
                                    nextButton(bundle, ids, Math.min(notesSize - count, 25), actionType),
                                    closeButton(bundle, ids, actionType));
                        } else {
                            actionRow = ActionRow.of(
                                    beforeButton(bundle, ids, count - 25, actionType),
                                    closeButton(bundle, ids, actionType));
                        }
                    }
                    switch (actionType) {
                        case SHOW ->
                                event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow).queue();
                        case EDIT, REMOVE ->
                                event.getHook().editOriginalEmbeds(showNoteEmbed.build()).setComponents(actionRow, ActionRow.of(menuBuilder.build())).queue();
                    }


                });
    }

    @JDAStringMenu(startWith = "noteCommand_showNoteEmbed_editMenu")
    public void noteEditSelectEvent(@NotNull StringSelectInteractionEvent event) {
        if (event.getGuild() == null) return;
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;
        TextInput.Builder noteContent = TextInput.create(event.getValues().get(0), "Note Content", TextInputStyle.PARAGRAPH);
        noteContent.setMaxLength(1024);

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(ids[1]), event.getGuild().getIdLong());
        noteContent.setValue(notes.getJSONObject(event.getValues().get(0)).getString("noteContent"));

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());

        Modal.Builder noteEditModal = Modal.create("noteCommand_editNoteModal;%s,%s".formatted(ids[0], ids[1]),
                bundle.getString("events.noteEditSelectEvent.editNoteModal.title"));
        noteEditModal.addComponents(ActionRow.of(noteContent.build()));
        event.getHook().deleteOriginal().queue();
        event.replyModal(noteEditModal.build()).queue();
    }

    @JDAStringMenu(startWith = "noteCommand_showNoteEmbed_removeMenu")
    public void noteRemoveSelectEvent(@NotNull StringSelectInteractionEvent event) {
        if (event.getGuild() == null) return;
        event.deferEdit().queue();
        String[] splitId = event.getComponentId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
        if (db.deleteDataFromNoteTable(
                Long.parseLong(ids[1]),
                Long.parseLong(ids[0]),
                event.getGuild().getIdLong(),
                Timestamp.valueOf(LocalDateTime.parse(event.getValues().get(0))))) {
            event.getHook().editOriginal(bundle.getString("events.noteRemoveSelectEvent.success"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        } else {
            event.getHook().editOriginal(bundle.getString("events.noteRemoveSelectEvent.failedToPutInDB"))
                    .setComponents()
                    .setEmbeds()
                    .queue();
        }
    }

    @JDAModal(startWith = "noteCommand_editNoteModal")
    public void noteModalEvent(@NotNull ModalInteractionEvent event) {
        if (event.getGuild() == null) return;
        event.deferReply(true).queue();
        String[] splitId = event.getModalId().split(";");
        String[] ids = splitId[1].split(",");
        if (!event.getUser().getId().equals(ids[0])) return;

        ResourceBundle bundle = Util.getResourceBundle("note", event.getUserLocale());
        ModalMapping noteContentField = event.getValues().get(0);

        JSONObject notes = db.getDataFromNoteTable(Long.parseLong(ids[1]), event.getGuild().getIdLong());

        if (notes.getJSONObject(noteContentField.getId()).getString("noteContent").equals(noteContentField.getAsString())) {
            event.getHook().editOriginal(bundle.getString("events.noteModalEvent.editIsEqualEntry")).queue();
            return;
        }

        if (db.updateDataFromNoteTable(
                noteContentField.getAsString(),
                Long.parseLong(ids[0]),
                Long.parseLong(ids[1]),
                event.getGuild().getIdLong(),
                Timestamp.valueOf(LocalDateTime.parse(noteContentField.getId())))) {
            event.getHook().editOriginal(bundle.getString("events.noteModalEvent.successMessage")).queue();
        } else {
            event.getHook().editOriginal(bundle.getString("events.noteModalEvent.failedMessage")).queue();
        }
    }

    private @NotNull Collection<RestAction<User>> setEmbedFieldsAndGetModerators(
            EmbedBuilder showNoteEmbed, StringSelectMenu.Builder menuBuilder, int countFrom, @NotNull JSONObject notes) {
        Collection<RestAction<User>> restActions = new ArrayList<>();
        int i = 0;

        for (String keyString : notes.keySet().stream().sorted().toList()) {
            countFrom--;
            if (countFrom < 0) {
                i++;
                JSONObject entry = notes.getJSONObject(keyString);
                String timeString = keyString.replace("T", " ").replaceAll(".\\d{5,}", " ");
                restActions.add(jda.retrieveUserById(entry.getLong("moderatorId"))
                        .onSuccess(user -> showNoteEmbed.addField(timeString + " from " + user.getAsTag(), entry.getString("noteContent"), false)));
                if (menuBuilder != null) menuBuilder.addOption(timeString, keyString);
                if (i == 25) break;
            }
        }
        return restActions;
    }

    private enum ClickType {
        NEXT,
        BEFORE
    }

    private enum NoteEmbedActionType {
        SHOW("show"),
        EDIT("edit"),
        REMOVE("remove");
        private final String name;

        NoteEmbedActionType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

}