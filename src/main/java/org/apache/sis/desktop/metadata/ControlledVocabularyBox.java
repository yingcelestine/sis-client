package org.apache.sis.desktop.metadata;

import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.opengis.util.ControlledVocabulary;

/**
 *
 * @author Siddhesh Rane
 */
public class ControlledVocabularyBox extends ComboBox<ControlledVocabulary> {

    public static final StringConverter<ControlledVocabulary> VOCABULARY_TO_STRING = new StringConverter<ControlledVocabulary>() {
        @Override
        public String toString(ControlledVocabulary object) {
            return object.name();
        }

        @Override
        public ControlledVocabulary fromString(String string) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    public ControlledVocabularyBox(ControlledVocabulary cv) {
        setConverter(VOCABULARY_TO_STRING);
        if (cv ==null) {
            return ;
        }
        getItems().setAll(cv.family());
        getSelectionModel().select(cv);
    }
    
}
