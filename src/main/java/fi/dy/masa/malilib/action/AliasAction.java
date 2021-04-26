package fi.dy.masa.malilib.action;

import java.util.List;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.input.ActionResult;
import fi.dy.masa.malilib.render.text.StyledTextLine;
import fi.dy.masa.malilib.util.JsonUtils;
import fi.dy.masa.malilib.util.data.ModInfo;

public class AliasAction extends NamedAction
{
    protected final String alias;
    protected final String originalRegistryName;
    protected final NamedAction originalAction;

    public AliasAction(String alias, NamedAction action)
    {
        super(action.mod, action.name, alias, action.translationKey, action.getAction());

        this.alias = alias;
        this.originalRegistryName = action.registryName;
        this.originalAction = action;
    }

    @Override
    public String getName()
    {
        return this.alias;
    }

    public String getOriginalRegistryName()
    {
        return this.originalRegistryName;
    }

    @Override
    public StyledTextLine getWidgetDisplayName()
    {
        String alias = this.alias;
        String originalName = this.name;
        String modName = this.mod.getModName();
        return StyledTextLine.translate("malilib.label.named_action_alias_entry_widget.name",
                                        alias, modName, originalName);
    }

    @Override
    public List<StyledTextLine> getHoverInfo()
    {
        List<StyledTextLine> list = this.originalAction.getHoverInfo();

        list.add(0, StyledTextLine.translate("malilib.hover_info.action.alias", this.alias));
        list.add(StyledTextLine.translate("malilib.hover_info.action.original_registry_name", this.originalRegistryName));

        return list;
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();
        JsonObject actionData = this.originalAction.toJson();

        obj.addProperty("reg_name", this.originalAction.getRegistryName());

        if (actionData != null)
        {
            obj.add("data", actionData);
        }

        return obj;
    }

    public static AliasAction aliasFromJson(ActionRegistry registry, String aliasName, JsonElement el)
    {
        NamedAction action = null;
        String regName = "?";

        if (el.isJsonObject())
        {
            JsonObject obj = el.getAsJsonObject();

            if (JsonUtils.hasString(obj, "reg_name"))
            {
                regName = JsonUtils.getString(obj, "reg_name");
                action = registry.getAction(regName);

                if (action != null && obj.has("data"))
                {
                    action = action.fromJson(obj.get("data"));
                }
            }
        }

        if (action == null)
        {
            // Preserve entries in the config file if a mod is temporarily disabled/removed, for example
            // FIXME use a DummyAction that contains the JsonObject?
            action = new NamedAction(ModInfo.NO_MOD, aliasName, regName, aliasName, (ctx) -> ActionResult.PASS);
        }

        return new AliasAction(aliasName, action);
    }
}
