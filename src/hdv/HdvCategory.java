package hdv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HdvCategory {

    private int categoryId;
    private Map<Integer, HdvTemplate> templates = new HashMap<Integer, HdvTemplate>();    //Dans le format <templateID,Template>

    public HdvCategory(int categoryId) {
        this.setCategoryId(categoryId);
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Map<Integer, HdvTemplate> getTemplates() {
        return templates;
    }

    public HdvTemplate getTemplate(int templateId) {
        return this.getTemplates().get(templateId);
    }

    public void addTemplate(int templateId, HdvEntry toAdd) {
        this.getTemplates().put(templateId, new HdvTemplate(templateId, toAdd));
    }

    public void delTemplate(int templateId) {
        this.getTemplates().remove(templateId);
    }

    public void addEntry(HdvEntry toAdd) {
        int templateId = toAdd.getGameObject().getTemplate().getId();
        if (this.getTemplates().get(templateId) == null)
            addTemplate(templateId, toAdd);
        else
            this.getTemplates().get(templateId).addEntry(toAdd);
    }

    public boolean delEntry(HdvEntry toDel) {
        boolean toReturn = false;
        this.getTemplates().get(toDel.getGameObject().getTemplate().getId()).delEntry(toDel);
        if ((toReturn = this.getTemplates().get(toDel.getGameObject().getTemplate().getId()).isEmpty()))
            delTemplate(toDel.getGameObject().getTemplate().getId());
        return toReturn;
    }

    public ArrayList<HdvEntry> getAllEntry() {
        ArrayList<HdvEntry> toReturn = new ArrayList<>();
        for (HdvTemplate template : this.getTemplates().values())
            toReturn.addAll(template.getAllEntry());
        return toReturn;
    }

    public String parseTemplate() {
        StringBuilder strTemplate = new StringBuilder();
        for (int templateId : this.getTemplates().keySet()) {
            if (!strTemplate.toString().isEmpty()) {
                strTemplate.append(";");
            }
            strTemplate.append(templateId);
        }
        return strTemplate.toString();
    }
}