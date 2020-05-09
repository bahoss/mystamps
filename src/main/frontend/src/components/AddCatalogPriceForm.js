//
// IMPORTANT:
// You must update ResourceUrl.RESOURCES_VERSION each time whenever you're modified this file!
//
// @todo #1342 AddCatalogPriceForm: show a currency

class AddCatalogPriceForm extends React.Component {
	constructor(props) {
		super(props);
		this.state = {
			price: null,
			catalog: 'michel',
			hasServerError: false,
			validationErrors: [],
			isDisabled: false
		};
		this.handleSubmit = this.handleSubmit.bind(this);
		this.handleChangePrice = this.handleChangePrice.bind(this);
		this.handleChangeCatalog = this.handleChangeCatalog.bind(this);
	}

	handleChangePrice(event) {
		event.preventDefault();
		this.setState({
			price: event.target.value
		});
	}

	handleChangeCatalog(event) {
		event.preventDefault();
		this.setState({
			catalog: event.target.value
		});
	}

	handleSubmit(event) {
		event.preventDefault();

		this.setState({
			isDisabled: true,
			hasServerError: false,
			validationErrors: []
		});

		axios.patch(
				this.props.url,
				[
					{
						op: 'add',
						path: `/${this.state.catalog}_price`,
						value: this.state.price
					}
				],
				{
					headers: {
						[this.props.csrfHeaderName]: this.props.csrfTokenValue,
						'Cache-Control': 'no-store'
					},
					validateStatus: status => {
						return status == 204 || status == 400;
					}
				}
			)
			.then(response => {
				const data = response.data;
				if (data.hasOwnProperty('fieldErrors')) {
					const fieldErrors = [];
					if (data.fieldErrors.price) {
						fieldErrors.push(...data.fieldErrors.price);
					}

					this.setState({
						isDisabled: false,
						validationErrors: fieldErrors
					});
					return;
				}

				// no need to reset the state as page will be reloaded
				window.location.reload();
			})
			.catch(error => {
				console.error(error);
				this.setState({ isDisabled: false, hasServerError: true });
			});
	}
	render() {
		const hasValidationErrors = this.state.validationErrors.length > 0;
		return (
			<div className="col-sm-12 form-group">
				<form className={`form-horizontal ${hasValidationErrors ? 'has-error' : ''}`} onSubmit={this.handleSubmit}>
					<div
						id="add-catalog-price-failed-msg"
						className={`alert alert-danger text-center col-sm-8 col-sm-offset-2 ${this.state.hasServerError ? '' : 'hidden'}`}>
						{ this.props.l10n['t_server_error'] || 'Server error' }
					</div>
					<div className="form-group form-group-sm">
						<label className="control-label col-sm-3">
							{ this.props.l10n['t_catalog'] || 'Catalog' }
						</label>
						<div className="col-sm-6">
							<select
								id="catalog-name"
								name="catalogName"
								className="form-control"
								onChange={this.handleChangeCatalog}>
								<option value="michel">
									{ this.props.l10n['t_michel'] || 'Michel' }
								</option>
								<option value="scott">
									{ this.props.l10n['t_scott'] || 'Scott' }
								</option>
								<option value="yvert">
									{ this.props.l10n['t_yvert'] || 'Yvert et Tellier' }
								</option>
								<option value="gibbons">
									{ this.props.l10n['t_sg'] || 'Stanley Gibbons' }
								</option>
								<option value="solovyov">
									{ this.props.l10n['t_solovyov'] || 'Solovyov' }
								</option>
								<option value="zagorski">
									{ this.props.l10n['t_zagorski'] || 'Zagorski' }
								</option>
							</select>
						</div>
					</div>
					<div className="form-group form-group-sm">
						<label className="control-label col-sm-3">
							{ this.props.l10n['t_price'] || 'Price' }
						</label>
						<div className="col-sm-6">
							<div className="row">
								<div className="col-xs-6">
									<input
										className="form-control"
										id="catalog-price"
										type="text"
										size="5"
										required="required"
										onChange={ this.handleChangePrice }/>
								</div>			
							</div>
						</div>
					</div>
					<div className="col-sm-offset-3 col-sm-4">
						<span
							id="catalog-price.errors"
							className={`help-block ${hasValidationErrors ? '' : 'hidden'}`}>
							{ this.state.validationErrors.join(', ') }
						</span>
						<button
							type="submit"
							className="btn btn-primary btn-sm"
							disabled={ this.state.isDisabled }>
							{ this.props.l10n['t_add'] || 'Add' }
						</button>
					</div>
				</form>
			</div>
		);
	}
}