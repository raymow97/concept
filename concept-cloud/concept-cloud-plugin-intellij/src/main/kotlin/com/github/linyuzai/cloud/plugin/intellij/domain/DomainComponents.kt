package com.github.linyuzai.cloud.plugin.intellij.domain

import com.github.linyuzai.cloud.plugin.intellij.panel
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogBuilder
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.IconButton
import com.intellij.ui.InplaceButton
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.layout.LCFlags
import com.intellij.util.ui.components.BorderLayoutPanel
import java.awt.Dimension
import java.awt.event.AdjustmentEvent
import java.awt.event.AdjustmentListener

object DomainComponents {

    @JvmStatic
    fun showGenerateDomainCodeDialog(project: Project, model: DomainModel): Boolean {
        val dialog = DialogBuilder(project)
        dialog.setTitle("Generate Domain Code")
        val panel = BorderLayoutPanel().apply {
            val dimension = Dimension(950, 500)
            //preferredSize = dimension
            minimumSize = dimension
        }

        panel.addToCenter(createGenerateDomainPanel(project, model))
        panel.addToRight(createPreviewDomainPanel(model))
        dialog.setCenterPanel(panel)
        model.preview()
        return dialog.showAndGet()
    }

    @JvmStatic
    fun createGenerateDomainPanel(project: Project, model: DomainModel): DialogPanel {

        return panel(LCFlags.fillX, LCFlags.fillY) {

            row("User Domain Class:") {
                classesComboBox(
                    project,
                    DomainModel.RECENTS_KEY_USER_DOMAIN_CLASS,
                    model.userClass
                ) {}
            }

            row("Domain Module (.main):") {
                modulesComboBox(project, model.domainModule)
            }

            row("Domain Package:") {
                packagesComboBox(
                    project,
                    DomainModel.RECENTS_KEY_DOMAIN_PACKAGE,
                    model.domainPackage
                )
            }

            row("Domain Class Name:") {
                textField(model.domainClassName)
            }

            row("Domain Class Props:") {
                val addButton = InplaceButton(
                    IconButton(
                        "Add prop",
                        AllIcons.General.InlineAdd, AllIcons.General.InlineAddHover
                    )
                ) {
                    model.addDomainProp()
                }
                component(addButton)
            }

            row {
                scrollPane(DomainPropsPanel(project).apply {
                    propRemoveListener = {
                        model.removeDomainProp(it.index)
                    }
                    model.addOnDomainPropAddListener { prop ->
                        addProp(prop)
                        model.preview()
                    }
                    model.addOnDomainPropRemoveListener { prop ->
                        removeProp(prop)
                        model.preview()
                    }
                }, minimumSize = Dimension(0, 200)) {
                    model.addOnDomainPropAddListener {
                        verticalScrollBar.apply {
                            addAdjustmentListener(object : AdjustmentListener {

                                override fun adjustmentValueChanged(e: AdjustmentEvent) {
                                    if (e.adjustmentType == AdjustmentEvent.TRACK) {
                                        removeAdjustmentListener(this)
                                        value = maximum - getModel().extent
                                    }
                                }
                            })
                        }
                    }
                }
                /*component(JPanel(GridBagLayout()).apply {
                    add(BorderLayoutPanel().apply {
                        minimumSize = Dimension(0, 200)
                        preferredSize = Dimension(0, 0)
                        addToCenter(ScrollPaneFactory.createScrollPane(DomainPropsPanel(project).apply {
                            propRemoveListener = {
                                model.removeDomainProp(it)
                                model.updateDomainProps()

                            }
                            model.addOnDomainPropUpdateListener {
                                update(it)
                            }
                        }))
                    }, gridConstraint(0, 0))
                }).constraints(push, grow)*/
            }
        }
    }

    fun createPreviewDomainPanel(model: DomainModel): DialogPanel {
        return panel(LCFlags.fillX, LCFlags.fillY) {
            row("Preview (pseudo code):") {

            }

            row {
                //scrollableTextArea({""},{})
                scrollPane(JBTextArea(1, 40).apply {
                    isEditable = false
                    model.domainPreview.afterChange {
                        text = it
                    }
                })
            }
        }
    }

    @JvmStatic
    fun createGenerateDomainAndModule(project: Project, model: DomainModel): DialogPanel {

        return panel(LCFlags.fillX, LCFlags.fillY) {

            row("Name:") {
                textField(model.domainClassName)
            }

            row("User Domain Class:") {
                /*classesComboBox(
                    project,
                    "GenerateDomainAndModule@User",
                    "Choose User Domain Class",
                    model.userClassNameProperty
                )*/
            }

            row("Domains Module:") {
                modulesComboBox(project, model.domainModule)
            }
        }
    }
}