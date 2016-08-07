package org.jetbrains.comparisonChain;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

class GenerateDialog extends DialogWrapper {
    private CollectionListModel<PsiField> myFields;
    private final LabeledComponent<JPanel> myComponent;

    GenerateDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields for Comparison");

        myFields = new CollectionListModel<>(psiClass.getAllFields());
        JBList fieldList = new JBList(myFields);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        final ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        final JPanel panel = decorator.createPanel();
        myComponent = LabeledComponent.create(panel, "Fields to include in CompareTo()");

        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myComponent;
    }

    List<PsiField> getFields() {
        return myFields.getItems();
    }
}
