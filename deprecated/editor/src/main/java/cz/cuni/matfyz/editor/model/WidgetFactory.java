package cz.cuni.matfyz.editor.model;

import cz.cuni.matfyz.editor.model.widgets.CategoricalObjectWidget;

/**
 *
 * @author pavel.koupil
 */
public class WidgetFactory {

	public Widget createWidget(String id, String name, double x, double y, WidgetType type) {

		Widget widget;

		switch (type) {

			case MAPPING_AVAILABLE_PROPERTY ->
				widget = new CategoricalObjectWidget(WidgetType.MAPPING_AVAILABLE_PROPERTY.name(), id, name, x, y, 50, 50);
			// todo: color, ... a dalsi! pokud neni color, tak se pouzije default color...
				
//			case MAPPING_AVAILABLE_KIND ->
//				widget = new AvailableKindCell(id, name, x, y);
//			case MAPPING_AVAILABLE_NAME ->
//				widget = new AvailableNameCell(id, name, x, y);
//			case MAPPING_SELECTED_NAME ->
//				widget = new SelectedNameCell(id, name, x, y);
//			case CATEGORICAL_OBJECT ->
//				widget = new CategoricalObjectCell(id, name, x, y);
//			case MAPPING_KIND ->
//				widget = new KindCell(id, name, x, y);
//			case MAPPING_PROPERTY ->
//				widget = new PropertyCell(id, name, x, y);
//			case MAPPING_SELECTED_KIND ->
//				widget = new SelectedKindCell(id, name, x, y);
//			case MAPPING_SELECTED_PROPERTY ->
//				widget = new SelectedPropertyObjectCell(id, name, x, y);
//			case MAPPING_AVAILABLE ->
//				widget = new AvailableObjectCell(id, name, x, y);
//			case MAPPING_NAME ->
//				widget = new NameCell(id, name, x, y);
//			case ER_ENTITY ->
//				widget = new EREntityCell(id, name, x, y);
//			case ER_RELATIONSHIP ->
//				widget = new ERRelationshipCell(id, name, x, y);
//			case ER_ATTRIBUTE ->
//				widget = new ERAttributeCell(id, name, x, y);
//			case ER_IDENTIFIER ->
//				widget = new ERIdentifierCell(id, name, x, y);
//			case ER_WEAK_IDENTIFIER ->
//				widget = new ERWeakIdentifierCell(id, name, x, y);
//			case SELECTED ->
//				widget = new SelectedCell(id, name, x, y);
//			case MONGODB_KIND ->
//				widget = new MongoDBKindCell(id, name, x, y);
//			case POSTGRESQL_KIND ->
//				widget = new PostgreSQLKindCell(id, name, x, y);
			default ->
				widget = null;
		}

		return widget;
	}

}
