import React from "react";
import { Link } from "react-router-dom";
import { Breadcrumb, BreadcrumbItem } from "@patternfly/react-core";

export interface BreadCrumbPathProps {
  breadcrumbs: { title: string; path: string }[];
}

export const BreadCrumbPath: React.FC<BreadCrumbPathProps> = ({
  breadcrumbs,
}) => {
  return (
    <Breadcrumb>
      {breadcrumbs.map((crumb, i, { length }) => {
        const isLast = i === length - 1;

        return (
          <BreadcrumbItem key={i} isActive={isLast}>
            {isLast ? (
              crumb.title
            ) : (
              <Link className="pf-c-breadcrumb__link" to={crumb.path}>
                {crumb.title}
              </Link>
            )}
          </BreadcrumbItem>
        );
      })}
    </Breadcrumb>
  );
};
