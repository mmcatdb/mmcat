package cz.cuni.matfyz.editor.utils;

/**
 *
 * @author pavel.koupil
 */
public abstract class Constants {

	public static enum PropertyName {
		INHERIT("Inherit"), USER_DEFINED("User Defined"), DYNAMIC("Dynamic");

		private String value;

		private PropertyName(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}

	public static enum Zoom {
		_50, _75, _100, _150, _200, _300, _400, _600, _800;
	}

}
