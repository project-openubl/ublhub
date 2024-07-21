import React, { useEffect, useReducer, useState } from "react";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router-dom";

import {
  Avatar,
  Brand,
  Button,
  ButtonVariant,
  Dropdown,
  DropdownItem,
  DropdownList,
  Masthead,
  MastheadBrand,
  MastheadContent,
  MastheadMain,
  MastheadToggle,
  MenuToggle,
  MenuToggleElement,
  PageToggleButton,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarGroup,
  ToolbarItem,
} from "@patternfly/react-core";

import BarsIcon from "@patternfly/react-icons/dist/js/icons/bars-icon";
import QuestionCircleIcon from "@patternfly/react-icons/dist/esm/icons/question-circle-icon";
import EllipsisVIcon from "@patternfly/react-icons/dist/esm/icons/ellipsis-v-icon";
import HelpIcon from "@patternfly/react-icons/dist/esm/icons/help-icon";

import i18n from "@app/i18n";
import { useLocalStorage } from "@app/hooks/useStorage";
import { APP_BRAND, BrandType, isAuthRequired } from "@app/Constants";

import { AboutApp } from "./about";
import openublBrandImage from "@app/images/Openubl-white-logo.svg";
import imgAvatar from "../images/avatar.svg";

export const HeaderApp: React.FC = () => {
  const auth = (isAuthRequired && useAuth()) || undefined;

  const navigate = useNavigate();

  const [isAboutOpen, toggleIsAboutOpen] = useReducer((state) => !state, false);
  const [isLangDropdownOpen, setIsLangDropdownOpen] = useState(false);
  const [isKebabDropdownOpen, setIsKebabDropdownOpen] = useState(false);
  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);

  const [lang, setLang] = useLocalStorage({ key: "lang", defaultValue: "es" });

  useEffect(() => {
    i18n.changeLanguage(lang);
  }, [lang]);

  const kebabDropdownItems = (
    <>
      <DropdownItem key="about" onClick={toggleIsAboutOpen}>
        <HelpIcon /> About
      </DropdownItem>
    </>
  );

  const onKebabDropdownToggle = () => {
    setIsKebabDropdownOpen(!isKebabDropdownOpen);
  };

  const onKebabDropdownSelect = () => {
    setIsKebabDropdownOpen(!isKebabDropdownOpen);
  };

  return (
    <>
      <AboutApp isOpen={isAboutOpen} onClose={toggleIsAboutOpen} />

      <Masthead>
        <MastheadToggle>
          <PageToggleButton variant="plain" aria-label="Global navigation">
            <BarsIcon />
          </PageToggleButton>
        </MastheadToggle>
        <MastheadMain>
          <MastheadBrand>
            {APP_BRAND === BrandType.Openubl ? (
              <Brand
                src={openublBrandImage}
                alt="brand"
                heights={{ default: "60px" }}
              />
            ) : (
              <Title className="logo-pointer" headingLevel="h1" size="2xl">
                LibreFact
              </Title>
            )}
          </MastheadBrand>
        </MastheadMain>
        <MastheadContent>
          <Toolbar id="toolbar" isFullHeight isStatic>
            <ToolbarContent>
              <ToolbarGroup
                variant="icon-button-group"
                align={{ default: "alignRight" }}
                spacer={{ default: "spacerNone", md: "spacerMd" }}
              >
                <ToolbarGroup
                  variant="icon-button-group"
                  visibility={{ default: "hidden", lg: "visible" }}
                >
                  <ToolbarItem>
                    <Dropdown
                      isOpen={isLangDropdownOpen}
                      onSelect={() =>
                        setIsLangDropdownOpen(!isLangDropdownOpen)
                      }
                      onOpenChange={(isOpen: boolean) =>
                        setIsLangDropdownOpen(isOpen)
                      }
                      popperProps={{ position: "right" }}
                      toggle={(toggleRef: React.Ref<MenuToggleElement>) => (
                        <MenuToggle
                          ref={toggleRef}
                          onClick={() =>
                            setIsLangDropdownOpen(!isLangDropdownOpen)
                          }
                          isExpanded={isLangDropdownOpen}
                          variant="plainText"
                          aria-label="About"
                        >
                          {lang}
                        </MenuToggle>
                      )}
                    >
                      <DropdownList>
                        <DropdownItem key="es" onClick={() => setLang("es")}>
                          Español
                        </DropdownItem>
                        <DropdownItem key="en" onClick={() => setLang("en")}>
                          Ingles
                        </DropdownItem>
                      </DropdownList>
                    </Dropdown>
                  </ToolbarItem>
                  <ToolbarItem>
                    <Button
                      aria-label="About"
                      variant={ButtonVariant.plain}
                      icon={<QuestionCircleIcon />}
                      onClick={toggleIsAboutOpen}
                    />
                  </ToolbarItem>
                </ToolbarGroup>
                <ToolbarItem
                  visibility={{
                    default: "hidden",
                    md: "visible",
                    lg: "hidden",
                  }}
                >
                  <Dropdown
                    isOpen={isKebabDropdownOpen}
                    onSelect={onKebabDropdownSelect}
                    onOpenChange={(isOpen: boolean) =>
                      setIsKebabDropdownOpen(isOpen)
                    }
                    popperProps={{ position: "right" }}
                    toggle={(toggleRef: React.Ref<MenuToggleElement>) => (
                      <MenuToggle
                        ref={toggleRef}
                        onClick={onKebabDropdownToggle}
                        isExpanded={isKebabDropdownOpen}
                        variant="plain"
                        aria-label="About"
                      >
                        <EllipsisVIcon aria-hidden="true" />
                      </MenuToggle>
                    )}
                  >
                    <DropdownList>{kebabDropdownItems}</DropdownList>
                  </Dropdown>
                </ToolbarItem>
                {auth && (
                  <ToolbarItem
                    visibility={{ default: "hidden", md: "visible" }}
                  >
                    <Dropdown
                      isOpen={isUserDropdownOpen}
                      onSelect={() => setIsUserDropdownOpen(false)}
                      onOpenChange={(isOpen: boolean) =>
                        setIsUserDropdownOpen(isOpen)
                      }
                      popperProps={{ position: "right" }}
                      toggle={(toggleRef: React.Ref<MenuToggleElement>) => (
                        <MenuToggle
                          ref={toggleRef}
                          onClick={() =>
                            setIsUserDropdownOpen(!isUserDropdownOpen)
                          }
                          isFullHeight
                          isExpanded={isUserDropdownOpen}
                          icon={<Avatar src={imgAvatar} alt="" />}
                        >
                          {auth.user?.profile.preferred_username}
                        </MenuToggle>
                      )}
                    >
                      <DropdownList>
                        <DropdownItem
                          key="logout"
                          onClick={() => {
                            auth
                              .signoutRedirect()
                              .then(() => {})
                              .catch((err) => {
                                console.error("Logout failed:", err);
                                navigate("/");
                              });
                          }}
                        >
                          Logout
                        </DropdownItem>
                      </DropdownList>
                    </Dropdown>
                  </ToolbarItem>
                )}
              </ToolbarGroup>
            </ToolbarContent>
          </Toolbar>
        </MastheadContent>
      </Masthead>
    </>
  );
};
